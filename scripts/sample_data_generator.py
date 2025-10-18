#!/usr/bin/env python3
"""
Taskify sample data generator.

Seeds demo data via the public REST API and supports API-driven cleanup.
"""

from __future__ import annotations

import argparse
import dataclasses
import json
import logging
import sys
import textwrap
import time
from collections import Counter
from dataclasses import dataclass, field
from datetime import date, timedelta
from itertools import cycle
from pathlib import Path
from typing import Any, Dict, Iterable, List, Optional, Sequence, Tuple
from uuid import UUID, uuid5

import requests
from requests import Session
from requests.exceptions import RequestException


LOG = logging.getLogger("taskify.seed")
NAMESPACE = UUID("11111111-2222-3333-4444-555555555555")


class ApiError(RuntimeError):
    """Raised when a REST request fails."""

    def __init__(self, method: str, url: str, status: int, message: str, body: Any = None):
        detail = f"{method} {url} -> {status} {message}"
        super().__init__(detail)
        self.method = method
        self.url = url
        self.status = status
        self.body = body


@dataclass
class SeedConfig:
    base_url: str
    actor_id: str
    owner_ids: List[str]
    assignee_ids: List[str]
    seed_file: Path
    run_mode: str
    dry_run: bool
    verbose: bool
    max_retries: int = 2
    retry_wait: float = 0.75


@dataclass
class SeedState:
    version: int = 1
    tags: List[Dict[str, Any]] = field(default_factory=list)
    projects: List[Dict[str, Any]] = field(default_factory=list)
    tasks: List[Dict[str, Any]] = field(default_factory=list)
    task_tags: List[Dict[str, Any]] = field(default_factory=list)
    comments: List[Dict[str, Any]] = field(default_factory=list)

    def to_dict(self) -> Dict[str, Any]:
        return dataclasses.asdict(self)

    @classmethod
    def from_dict(cls, data: Dict[str, Any]) -> "SeedState":
        state = cls()
        for field_name in ("tags", "projects", "tasks", "task_tags", "comments"):
            setattr(state, field_name, data.get(field_name, []))
        return state


class StateManager:
    """Persists created resource ids for later cleanup."""

    def __init__(self, path: Path):
        self.path = path
        self.state = SeedState()

    def load(self) -> None:
        if not self.path.exists():
            raise FileNotFoundError(f"Seed state file not found: {self.path}")
        with self.path.open("r", encoding="utf-8") as fh:
            data = json.load(fh)
        self.state = SeedState.from_dict(data)

    def reset(self) -> None:
        self.state = SeedState()

    def save(self) -> None:
        self.path.parent.mkdir(parents=True, exist_ok=True)
        payload = self.state.to_dict()
        payload["savedAt"] = time.strftime("%Y-%m-%dT%H:%M:%SZ", time.gmtime())
        with self.path.open("w", encoding="utf-8") as fh:
            json.dump(payload, fh, indent=2)


class ApiClient:
    """Thin wrapper around requests with basic retries and logging."""

    def __init__(self, cfg: SeedConfig):
        self.cfg = cfg
        self.session = Session()
        self.session.headers.update(
            {
                "Accept": "application/json",
                "Content-Type": "application/json",
                "X-Actor-Id": cfg.actor_id,
            }
        )

    def _url(self, path: str) -> str:
        if path.startswith("http://") or path.startswith("https://"):
            return path
        if not path.startswith("/"):
            path = "/" + path
        return self.cfg.base_url.rstrip("/") + path

    def request(self, method: str, path: str, *, json_body: Any = None, expect_json: bool = True) -> Any:
        url = self._url(path)
        method_upper = method.upper()

        if self.cfg.dry_run and method_upper in {"POST", "PUT", "PATCH", "DELETE"}:
            LOG.info("[dry-run] %s %s %s", method_upper, url, json_body or "")
            pseudo_id = str(uuid5(NAMESPACE, f"{method_upper}:{url}:{json_body}"))
            # Return stub for create endpoints that expect JSON response.
            if method_upper in {"POST", "PUT"} and expect_json:
                return {"id": pseudo_id}
            return None

        payload = json.dumps(json_body) if json_body is not None else None

        for attempt in range(1, self.cfg.max_retries + 2):
            try:
                response = self.session.request(method_upper, url, data=payload, timeout=30)
            except RequestException as exc:
                if attempt > self.cfg.max_retries:
                    raise RuntimeError(f"HTTP request failed: {method_upper} {url}: {exc}") from exc
                LOG.warning("Request error (%s %s): %s (retry %s/%s)", method_upper, url, exc, attempt, self.cfg.max_retries)
                time.sleep(self.cfg.retry_wait * attempt)
                continue

            if response.status_code >= 400:
                snippet = response.text[:500]
                raise ApiError(method_upper, url, response.status_code, response.reason, snippet)

            if expect_json:
                if response.content:
                    return response.json()
                return None

            return response

        raise RuntimeError("Unreachable")  # pragma: no cover

    def get(self, path: str) -> Any:
        return self.request("GET", path, expect_json=True)

    def post(self, path: str, body: Any) -> Any:
        return self.request("POST", path, json_body=body, expect_json=True)

    def put(self, path: str, body: Any) -> Any:
        return self.request("PUT", path, json_body=body, expect_json=True)

    def patch(self, path: str, body: Any) -> Any:
        return self.request("PATCH", path, json_body=body, expect_json=True)

    def delete(self, path: str) -> Any:
        return self.request("DELETE", path, expect_json=False)


def parse_args(argv: Optional[Sequence[str]] = None) -> SeedConfig:
    parser = argparse.ArgumentParser(
        description="Seed sample Taskify data via REST API.",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog=textwrap.dedent(
            """
            Examples:
              python3 scripts/sample_data_generator.py --actor-id <uuid> --owner-ids <uuid>,<uuid> --run create
              python3 scripts/sample_data_generator.py --actor-id <uuid> --run cleanup
            """
        ),
    )
    parser.add_argument("--base-url", default="http://localhost:8081", help="Taskify API base URL (default: %(default)s)")
    parser.add_argument("--actor-id", help="User UUID for X-Actor-Id header")
    parser.add_argument("--owner-ids", help="Comma-separated UUIDs to assign as project owners (optional)")
    parser.add_argument("--assignee-ids", help="Comma-separated UUIDs to rotate for task assignees (optional)")
    parser.add_argument("--seed-file", default=".taskify-seed-state.json", help="State file path for tracking created resources")
    parser.add_argument("--run", choices=["create", "cleanup", "create-and-cleanup"], default="create", help="Action to perform")
    parser.add_argument("--dry-run", action="store_true", help="Log intended actions without mutating data")
    parser.add_argument("--verbose", action="store_true", help="Enable debug logging")
    parser.add_argument("--max-retries", type=int, default=2, help="Retry attempts for failed HTTP calls (default: %(default)s)")
    parser.add_argument("--retry-wait", type=float, default=0.75, help="Base wait seconds between retries (default: %(default)s)")

    args = parser.parse_args(argv)

    if not args.actor_id and not args.dry_run:
        parser.error("--actor-id is required unless --dry-run is set (API needs authentication)")

    owner_ids = split_csv(args.owner_ids)
    assignee_ids = split_csv(args.assignee_ids) if args.assignee_ids else owner_ids.copy()

    cfg = SeedConfig(
        base_url=args.base_url,
        actor_id=args.actor_id or "",
        owner_ids=owner_ids,
        assignee_ids=assignee_ids,
        seed_file=Path(args.seed_file),
        run_mode=args.run,
        dry_run=args.dry_run,
        verbose=args.verbose,
        max_retries=args.max_retries,
        retry_wait=args.retry_wait,
    )
    return cfg


def split_csv(value: Optional[str]) -> List[str]:
    if not value:
        return []
    return [item.strip() for item in value.split(",") if item.strip()]


def setup_logging(verbose: bool) -> None:
    level = logging.DEBUG if verbose else logging.INFO
    logging.basicConfig(level=level, format="%(levelname)s %(message)s")


def fetch_users(client: ApiClient) -> List[Dict[str, Any]]:
    LOG.debug("Fetching users...")
    users = client.get("/api/users")
    LOG.debug("Found %s users", len(users))
    return users


def resolve_user_ids(cfg: SeedConfig, client: ApiClient) -> Tuple[List[str], List[str], Dict[str, Dict[str, Any]]]:
    """Ensure owner/assignee IDs are populated, fetching from API if not provided."""
    user_cache = {user["id"]: user for user in fetch_users(client)}

    if not user_cache:
        raise RuntimeError("No users returned from /api/users; cannot seed data.")

    owners = cfg.owner_ids
    if not owners:
        owners = list(user_cache.keys())[:2]
        LOG.info("Using first available users as owners: %s", owners)

    assignees = cfg.assignee_ids or owners

    missing = [uid for uid in owners + assignees if uid not in user_cache]
    if missing:
        raise RuntimeError(f"Provided user IDs were not found via /api/users: {missing}")

    return owners, assignees, user_cache


def build_project_blueprint(owner_ids: Sequence[str]) -> List[Dict[str, Any]]:
    today = date.today()
    owners_cycle = cycle(owner_ids)
    projects = [
        {
            "slug": "project-atlas",
            "name": "Project Atlas",
            "description": "Modernize internal workflow engine and integrations.",
            "status": "ACTIVE",
            "start_date": today.isoformat(),
            "end_date": (today + timedelta(days=45)).isoformat(),
            "owner_id": next(owners_cycle),
        },
        {
            "slug": "project-borealis",
            "name": "Project Borealis",
            "description": "Launch customer-facing analytics portal MVP.",
            "status": "PLANNED",
            "start_date": (today + timedelta(days=3)).isoformat(),
            "end_date": (today + timedelta(days=60)).isoformat(),
            "owner_id": next(owners_cycle),
        },
    ]
    return projects


def build_task_blueprint(project_slug: str, assignee_ids: Sequence[str]) -> List[Dict[str, Any]]:
    assignees_cycle = cycle(assignee_ids)
    due_base = date.today() + timedelta(days=7)

    if project_slug == "project-atlas":
        return [
            {
                "title": "Atlas - Establish architecture runway",
                "description": "Document target service boundaries and integration points.",
                "priority": "HIGH",
                "status": "IN_PROGRESS",
                "assignee_id": next(assignees_cycle),
                "due_date": (due_base).isoformat(),
                "tags": ["Demo Backend"],
                "comments": [
                    "Kickoff meeting scheduled with platform team.",
                    "Awaiting confirmation on API contract changes.",
                ],
            },
            {
                "title": "Atlas - Platform access provisioning",
                "description": "Request credentials for staging environments.",
                "priority": "MEDIUM",
                "status": "TODO",
                "assignee_id": next(assignees_cycle),
                "due_date": (due_base + timedelta(days=5)).isoformat(),
                "tags": ["Demo UX"],
                "comments": [
                    "Need to coordinate with DevOps for VPN access.",
                    "Track progress in shared sheet.",
                ],
            },
            {
                "title": "Atlas - Feature flag scaffolding",
                "description": "Implement LaunchDarkly flags for phased rollout.",
                "priority": "CRITICAL",
                "status": "IN_PROGRESS",
                "assignee_id": next(assignees_cycle),
                "due_date": (due_base + timedelta(days=9)).isoformat(),
                "tags": ["Demo Backend", "Demo Critical"],
                "comments": [
                    "Confirm naming convention with release engineering.",
                    "Ensure fallbacks defaults are safe.",
                ],
            },
            {
                "title": "Atlas - Pilot feedback synthesis",
                "description": "Summarize insights from beta stakeholders.",
                "priority": "LOW",
                "status": "TODO",
                "assignee_id": next(assignees_cycle),
                "due_date": (due_base + timedelta(days=14)).isoformat(),
                "tags": ["Demo UX"],
                "comments": [
                    "Prepare template slides for weekly readout.",
                    "Coordinate with research team for interview notes.",
                ],
            },
        ]

    # Default blueprint for other projects
    return [
        {
            "title": "Borealis - Requirements alignment",
            "description": "Consolidate analytics KPIs with product owners.",
            "priority": "HIGH",
            "status": "TODO",
            "assignee_id": next(assignees_cycle),
            "due_date": (due_base + timedelta(days=3)).isoformat(),
            "tags": ["Demo UX"],
            "comments": [
                "Schedule workshop with marketing stakeholders.",
                "Identify success metrics for MVP launch.",
            ],
        },
        {
            "title": "Borealis - Data pipeline spike",
            "description": "Prototype ingestion pipeline for usage events.",
            "priority": "CRITICAL",
            "status": "IN_PROGRESS",
            "assignee_id": next(assignees_cycle),
            "due_date": (due_base + timedelta(days=8)).isoformat(),
            "tags": ["Demo Backend", "Demo Critical"],
            "comments": [
                "Evaluate storage costs for Redshift vs. BigQuery.",
                "Assess backfill approach for historical data.",
            ],
        },
        {
            "title": "Borealis - Dashboard wireframes",
            "description": "Deliver first iteration of customer analytics UI.",
            "priority": "MEDIUM",
            "status": "TODO",
            "assignee_id": next(assignees_cycle),
            "due_date": (due_base + timedelta(days=12)).isoformat(),
            "tags": ["Demo UX"],
            "comments": [
                "Review visual guidelines with design systems team.",
                "Collect feedback from pilot customers.",
            ],
        },
        {
            "title": "Borealis - Access control model",
            "description": "Define RBAC matrix for portal roles.",
            "priority": "HIGH",
            "status": "TODO",
            "assignee_id": next(assignees_cycle),
            "due_date": (due_base + timedelta(days=16)).isoformat(),
            "tags": ["Demo Backend"],
            "comments": [
                "Include enterprise admin scenarios.",
                "Draft Audit requirements for compliance review.",
            ],
        },
    ]


TAG_BLUEPRINT = [
    {"name": "Demo Critical", "color": "#EF4444"},
    {"name": "Demo UX", "color": "#3B82F6"},
    {"name": "Demo Backend", "color": "#10B981"},
]


class SeedRunner:
    def __init__(self, cfg: SeedConfig):
        self.cfg = cfg
        self.client = ApiClient(cfg)
        self.state_mgr = StateManager(cfg.seed_file)
        self.summary = Counter()

    # ----------------------------
    # Create flow
    # ----------------------------

    def run_create(self) -> None:
        if not self.cfg.dry_run:
            self.state_mgr.reset()

        owners, assignees, user_cache = resolve_user_ids(self.cfg, self.client)
        LOG.info("Owners: %s", owners)
        LOG.info("Assignees: %s", assignees)

        tag_id_map = self.ensure_tags()
        project_map = self.ensure_projects(owners)
        tasks_map = self.ensure_tasks(project_map, assignees)
        self.ensure_tag_attachments(tasks_map, tag_id_map)
        self.ensure_comments(tasks_map, user_cache)
        self.verify_activity(project_map)

        if not self.cfg.dry_run:
            self.state_mgr.save()

        self.print_summary()

    def ensure_tags(self) -> Dict[str, str]:
        existing = {tag["name"]: tag["id"] for tag in self.client.get("/api/tags")}
        tag_id_map = {}

        for tag in TAG_BLUEPRINT:
            name = tag["name"]
            if name in existing:
                tag_id = existing[name]
                LOG.info("Tag exists: %s (%s)", name, tag_id)
            else:
                LOG.info("Creating tag: %s", name)
                response = self.client.post("/api/tags", tag)
                tag_id = response.get("id")
                self.summary["tags_created"] += 1
                if not self.cfg.dry_run:
                    self.state_mgr.state.tags.append({"id": tag_id, "name": name})

            tag_id_map[name] = tag_id

        return tag_id_map

    def ensure_projects(self, owner_ids: Sequence[str]) -> Dict[str, Dict[str, Any]]:
        existing = {proj["name"]: proj for proj in self.client.get("/api/projects")}
        project_map: Dict[str, Dict[str, Any]] = {}

        for project in build_project_blueprint(owner_ids):
            name = project["name"]
            if name in existing:
                project_id = existing[name]["id"]
                LOG.info("Project exists: %s (%s)", name, project_id)
                project_map[project["slug"]] = existing[name]
                continue

            body = {
                "name": name,
                "description": project["description"],
                "ownerId": project["owner_id"],
                "status": project["status"],
                "startDate": project["start_date"],
                "endDate": project["end_date"],
            }
            LOG.info("Creating project: %s", name)
            response = self.client.post("/api/projects", body)
            project_id = response.get("id")
            project_record = {"id": project_id, **project}
            project_map[project["slug"]] = project_record
            self.summary["projects_created"] += 1

            if not self.cfg.dry_run:
                self.state_mgr.state.projects.append({"id": project_id, "name": name})

        return project_map

    def ensure_tasks(
        self, project_map: Dict[str, Dict[str, Any]], assignee_ids: Sequence[str]
    ) -> Dict[str, List[Dict[str, Any]]]:
        tasks_map: Dict[str, List[Dict[str, Any]]] = {}

        for slug, project in project_map.items():
            project_id = project["id"]
            existing_tasks = self.client.get(f"/api/tasks?projectId={project_id}")
            existing_by_title = {task["title"]: task for task in existing_tasks}
            desired_tasks = build_task_blueprint(slug, assignee_ids)
            tasks_map[slug] = []

            for task in desired_tasks:
                title = task["title"]
                if title in existing_by_title:
                    LOG.info("Task exists: %s", title)
                    task_obj = existing_by_title[title]
                else:
                    body = {
                        "projectId": project_id,
                        "title": title,
                        "description": task["description"],
                        "priority": task["priority"],
                        "status": task["status"],
                        "assigneeId": task["assignee_id"],
                        "dueDate": task["due_date"],
                    }
                    LOG.info("Creating task: %s", title)
                    task_obj = self.client.post("/api/tasks", body)
                    self.summary["tasks_created"] += 1
                    if not self.cfg.dry_run:
                        self.state_mgr.state.tasks.append({"id": task_obj["id"], "projectId": project_id, "title": title})

                task_obj = {**task_obj, "desired": task}
                tasks_map[slug].append(task_obj)

        return tasks_map

    def ensure_tag_attachments(
        self,
        tasks_map: Dict[str, List[Dict[str, Any]]],
        tag_id_map: Dict[str, str],
    ) -> None:
        for tasks in tasks_map.values():
            for task in tasks:
                task_id = task["id"]
                desired = task["desired"]
                for tag_name in desired["tags"]:
                    tag_id = tag_id_map[tag_name]
                    try:
                        LOG.info("Attaching tag %s to task %s", tag_name, task["title"])
                        self.client.post(f"/api/tasks/{task_id}/tags/{tag_id}", {})
                        self.summary["tag_attachments"] += 1
                        if not self.cfg.dry_run:
                            self.state_mgr.state.task_tags.append({"taskId": task_id, "tagId": tag_id})
                    except ApiError as err:
                        if err.status == 409:
                            LOG.info("Tag already attached (%s)", tag_name)
                        else:
                            raise

    def ensure_comments(
        self,
        tasks_map: Dict[str, List[Dict[str, Any]]],
        user_cache: Dict[str, Dict[str, Any]],
    ) -> None:
        for tasks in tasks_map.values():
            for task in tasks:
                task_id = task["id"]
                desired_comments = task["desired"]["comments"]
                existing_comments = self.client.get(f"/api/tasks/{task_id}/comments")
                existing_bodies = {comment["body"]: comment for comment in existing_comments}

                for comment_body in desired_comments:
                    if comment_body in existing_bodies:
                        LOG.info("Comment already exists for task %s", task["title"])
                        continue

                    comment_payload = {"body": comment_body}
                    LOG.info("Adding comment to task %s", task["title"])
                    response = self.client.post(f"/api/tasks/{task_id}/comments", comment_payload)
                    self.summary["comments_created"] += 1
                    if not self.cfg.dry_run:
                        self.state_mgr.state.comments.append({"id": response["id"], "taskId": task_id})

    def verify_activity(self, project_map: Dict[str, Dict[str, Any]]) -> None:
        for slug, project in project_map.items():
            project_id = project["id"]
            LOG.info("Checking activity feed for project %s", project["name"])
            try:
                activity = self.client.get(f"/api/activity?entityType=PROJECT&entityId={project_id}")
                LOG.debug("Activity entries for %s: %s", slug, len(activity))
                self.summary["activity_checks"] += 1
            except ApiError as err:
                LOG.warning("Unable to fetch activity for project %s: %s", project["name"], err)

    # ----------------------------
    # Cleanup flow
    # ----------------------------

    def run_cleanup(self) -> None:
        try:
            self.state_mgr.load()
        except FileNotFoundError as exc:
            raise RuntimeError(f"Cannot cleanup without state file: {self.cfg.seed_file}") from exc

        state = self.state_mgr.state

        # Delete comments
        for comment in state.comments:
            comment_id = comment["id"]
            task_id = comment["taskId"]
            LOG.info("Deleting comment %s", comment_id)
            try:
                self.client.delete(f"/api/tasks/{task_id}/comments/{comment_id}")
                self.summary["comments_deleted"] += 1
            except ApiError as err:
                if err.status == 404:
                    LOG.info("Comment already missing: %s", comment_id)
                else:
                    raise

        # Detach tags
        for item in state.task_tags:
            task_id = item["taskId"]
            tag_id = item["tagId"]
            LOG.info("Detaching tag %s from task %s", tag_id, task_id)
            try:
                self.client.delete(f"/api/tasks/{task_id}/tags/{tag_id}")
                self.summary["tag_detachments"] += 1
            except ApiError as err:
                if err.status == 404:
                    LOG.info("Attachment already removed: task=%s tag=%s", task_id, tag_id)
                else:
                    raise

        # Cancel tasks
        for task in state.tasks:
            task_id = task["id"]
            LOG.info("Marking task %s as CANCELLED", task_id)
            try:
                self.client.patch(f"/api/tasks/{task_id}/status", {"status": "CANCELLED"})
                self.summary["tasks_cancelled"] += 1
            except ApiError as err:
                if err.status == 404:
                    LOG.info("Task already removed: %s", task_id)
                else:
                    raise

        # Cancel projects
        for project in state.projects:
            project_id = project["id"]
            LOG.info("Marking project %s as CANCELLED", project["name"])
            try:
                self.client.patch(f"/api/projects/{project_id}/status", {"status": "CANCELLED"})
                self.summary["projects_cancelled"] += 1
            except ApiError as err:
                if err.status == 404:
                    LOG.info("Project already removed: %s", project_id)
                else:
                    raise

        self.print_summary()

    # ----------------------------
    # Utilities
    # ----------------------------

    def print_summary(self) -> None:
        if not self.summary:
            LOG.info("No changes were applied.")
            return
        LOG.info("Summary:")
        for key, count in sorted(self.summary.items()):
            LOG.info("  %s: %s", key, count)


def main(argv: Optional[Sequence[str]] = None) -> int:
    cfg = parse_args(argv)
    setup_logging(cfg.verbose)

    runner = SeedRunner(cfg)

    try:
        if cfg.run_mode == "create":
            runner.run_create()
        elif cfg.run_mode == "cleanup":
            runner.run_cleanup()
        else:  # create-and-cleanup
            runner.run_create()
            runner.run_cleanup()
    except ApiError as err:
        LOG.error("API error: %s (%s)", err, err.body)
        return 2
    except Exception as exc:  # pylint: disable=broad-except
        LOG.error("Error: %s", exc)
        return 1
    finally:
        runner.client.session.close()

    return 0


if __name__ == "__main__":
    sys.exit(main())
