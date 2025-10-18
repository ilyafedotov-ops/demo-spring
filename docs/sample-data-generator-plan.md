# Sample Data Generator Plan

## Goal
Build a repeatable script that seeds realistic demo data across the Taskify API (projects, tasks, comments, tags, activity) and verifies endpoint health by making live requests against a running instance.

## Assumptions
- Spring Boot app and PostgreSQL are already running locally (`.env` / docker-compose settings).
- API base URL defaults to `http://localhost:8081`.
- Authentication relies on the `X-Actor-Id` header; at least one valid user **already exists** (API lacks user creation). The script will reference this UUID via configuration.
- Python 3.11+ available for scripting; `requests` library can be installed locally.
- Script execution targets **local demo scenarios** by default (single environment, developer laptop). Configuration will still allow pointing to other hosts if needed.

## Detailed Tasks

1. **Catalog REST contracts and auth requirements** *(Completed)*
   - Inventory controllers under `src/main/java/com/example/taskify/web` for route patterns and methods.
   - Note headers enforced by `ActorHeaderAuthenticationFilter`.

2. **Capture domain enums and validation rules** *(Completed)*
   - Collect allowable values from `ProjectStatus`, `TaskStatus`, `TaskPriority`, and related records.
   - Record required fields from DTO records (e.g., `@NotBlank`, `@NotNull`).

3. **Decide on user provisioning strategy** *(Completed)*
   - Decision: rely solely on the public REST API surface; no direct database writes.
   - Requirement: operator must supply one or more existing user UUIDs (for project owners, task assignees, actor header).
   - Document optional helper steps for discovering users (e.g., call `GET /api/users` first).

4. **Outline sample dataset and dependency order**
   - Define the number of demo users, projects, tasks, tags, and comments.
   - Map relationships (e.g., each project gets N tasks, each task gets tags/comments).
   - Identify data needed for activity log queries.

5. **Select scripting stack and structure**
   - Confirm Python `requests` usage (Context7 quickstart reference).
   - Draft module layout (config loader, API client, data builders, orchestrator, CLI entrypoint).
   - Provide CLI flags tuned for local demos (e.g., `--base-url`, `--actor-id`, `--sample-size`).

6. **Define creation routines per entity**
   - Draft payload templates honouring domain constraints.
   - Implement idempotency keys/guards (e.g., deterministic names, skip on HTTP 409).
   - Add retry logic for transient failures.
   - Pair every create flow with its teardown counterpart using available API endpoints (e.g., delete comments, detach tags, mark projects/tasks cancelled when DELETE is unavailable).

7. **Plan verification steps**
   - After each creation, perform `GET` checks to confirm persistence.
   - Fetch activity log entries to ensure audit trail coverage.
   - Record summaries for console output.

8. **Design cleanup/re-run safeguards**
   - Provide explicit teardown commands that use REST endpoints only (e.g., `DELETE /api/tasks/{taskId}/comments/{commentId}`, detach tags, status transitions).
   - For entities without DELETE support, prefer reversible state changes (e.g., set projects to `CANCELLED`, tasks to `CANCELLED`) and reuse deterministic identifiers to avoid duplication.
   - Ensure script can be safely re-executed without duplicating data (idempotent naming/tagging).

9. **Document execution flow and prerequisites**
   - Write README section detailing environment setup, command usage, and configurable arguments (base URL, actor ID, sample sizes).
   - Highlight teardown commands and state-reset behaviour for local demos.
   - Reference integration with the web diagnostics UI for manual verification.

## Deliverables
- `scripts/sample_data_generator.py` (working title) with CLI entrypoint.
- Documentation updates outlining usage and safety considerations.
- Optional SQL or configuration snippets if user bootstrap is required.

## Open Questions
- Should the script also seed activity entries directly, or rely on API side-effects? (Prefer API side-effects for consistency.)
- How to best signal success/failure of reversible teardown when DELETE endpoints are unavailable?
- Do we need alternate configuration profiles for CI while keeping local-demo defaults?

## Sample Dataset Blueprint

| Entity   | Target Count | Dependencies                    | Notes                                                                 |
|----------|--------------|---------------------------------|-----------------------------------------------------------------------|
| Users    | 3 existing   | Supplied via config             | Script will read `GET /api/users` to map friendly names → UUIDs.      |
| Tags     | 3 new        | Actor user                      | Names: `Demo Critical`, `Demo UX`, `Demo Backend`; deterministic slug.|
| Projects | 2 new        | Owner UUID, actor UUID          | `Project Atlas`, `Project Borealis`; uses staggered start/end dates.  |
| Tasks    | 8 new        | Project IDs, optional assignees | 4 tasks per project covering status transitions and priorities.       |
| Comments | 16 new       | Task IDs, actor UUIDs           | 2 comments per task, alternating authors to exercise auditing.        |
| Activity | n/a          | Generated by side-effects       | Query `/api/activity` after seeding to confirm log entries exist.     |

### Creation Order
1. Verify/resolve users (`GET /api/users`) and map to roles (owner, actor, reviewers).
2. Create tags.
3. Create projects (assign alternating owners).
4. Create tasks per project (vary statuses, priorities, due dates).
5. Attach tags to tasks (one or two each).
6. Post comments (two different authors).
7. Trigger status transitions (optional) to enrich activity feed.
8. Query activity endpoints for verification.

### Local Demo Defaults
- Owners: first two users returned by `/api/users`.
- Assignees: rotate through configured user IDs.
- Time windows: start date = today, end date = +30 days; due dates offset by task index.

## Script Architecture & Flow

```
scripts/sample_data_generator.py
├─ main(): CLI entrypoint (argparse)
├─ load_config(): merges CLI args + env vars
├─ ApiClient: wraps `requests` with base URL, headers, retry, logging
├─ DatasetPlanner: prepares deterministic IDs/names based on config
├─ SeedOperations:
│   ├─ seed_tags()
│   ├─ seed_projects()
│   ├─ seed_tasks()
│   ├─ seed_comments()
│   ├─ perform_status_updates()
│   └─ verify_activity()
├─ CleanupOperations:
│   ├─ teardown_comments()
│   ├─ detach_tags()
│   ├─ cancel_tasks()
│   └─ cancel_projects()
└─ report_summary(): print human-friendly results
```

### Key Modules
- `config`: parse `--base-url`, `--actor-id`, `--owner-ids`, `--dry-run`, `--cleanup`.
- `http`: shared API call helper with automatic JSON decode and error wrapping.
- `datasets`: deterministic payload builders (e.g., `build_project_payload(index)`).
- `runner`: orchestrates create → verify → report; also supports `--cleanup-only`.
- `utils`: UUID/name helpers, timestamp generation, exponential backoff.

## Entity Operations & Teardown

| Entity   | Create Endpoint                        | Idempotency Strategy                                  | Teardown Endpoint / Strategy                               |
|----------|----------------------------------------|-------------------------------------------------------|------------------------------------------------------------|
| Tag      | `POST /api/tags`                       | Check existing via `GET /api/tags` by name            | `DELETE` not available → detach from tasks, reuse names    |
| Project  | `POST /api/projects`                   | Deterministic name; skip if `GET` finds existing name | `PATCH /api/projects/{id}/status` → set `CANCELLED`        |
| Task     | `POST /api/tasks`                      | Deterministic title per project+index                 | `PATCH /api/tasks/{id}/status` → set `CANCELLED`           |
| Task Tag | `POST /api/tasks/{taskId}/tags/{tagId}`| Guard repeated attach via cached pairs                | `DELETE /api/tasks/{taskId}/tags/{tagId}`                  |
| Comment  | `POST /api/tasks/{taskId}/comments`    | Hash body to skip duplicates                          | `DELETE /api/tasks/{taskId}/comments/{commentId}`          |

### Additional Considerations
- Track created resource IDs in memory + JSON artifact (`.taskify-seed-state.json`) to reuse during teardown.
- Wrap status-changing calls in helper that validates allowed transitions before issuing request.
- Provide `--max-retries` and `--retry-wait` options for transient network errors.

## Verification & Reporting
- After each create phase, run corresponding `GET` endpoint and assert counts.
- Run `GET /api/activity?entityType=PROJECT` / `TASK` to ensure log entries exist.
- Summaries include:
  - counts created/skipped
  - endpoints exercised
  - failures with HTTP status and payload excerpt
- Optional `--verbose` flag to dump raw JSON.

## Cleanup & Re-run Strategy
- `--cleanup` triggers teardown sequence only (read state file, call delete/detach/cancel).
- Default seeding run performs cleanup when `--cleanup-after` flag is enabled.
- Idempotent naming allows repeated runs without clashes; script short-circuits create calls when resources already exist.
- Provide warning when cancellation leaves data in place (since DELETE unavailable) and point users to diagnostics UI for manual inspection.

## Execution & Usage Notes

### Prerequisites
- Local Taskify services running (`docker-compose.local.yml` or IDE).
- Actor UUID available (e.g., copy from web UI diagnostics or list via `GET /api/users`).
- Python environment with `requests>=2.31`; install via `python -m pip install requests`.
- Optional: create a virtual environment dedicated to tooling scripts.

### Recommended Workflow
1. **Discover users** (once): `curl -H "X-Actor-Id: <uuid>" http://localhost:8081/api/users` to gather owner/assignee IDs.
2. **Seed demo data**:
   ```bash
   python3 scripts/sample_data_generator.py \
     --base-url http://localhost:8081 \
     --actor-id 11111111-2222-3333-4444-555555555555 \
     --owner-ids 11111111-2222-3333-4444-555555555555,22222222-3333-4444-5555-666666666666 \
     --run create
   ```
3. **Verify via diagnostics UI**: open `http://localhost:8081/`, switch to Health tab, run diagnostics to confirm all endpoints succeed with seeded data.
4. **Teardown (optional)**:
   ```bash
   python3 scripts/sample_data_generator.py --run cleanup
   ```
   This reads `.taskify-seed-state.json` and reverses operations (delete comments, detach tags, cancel tasks/projects).

### CLI Flags (draft)
- `--base-url` (default `http://localhost:8081`)
- `--actor-id` (required; used for auth header)
- `--owner-ids` (comma-separated list; rotates across projects/tasks)
- `--assignee-ids` (optional override; defaults to `owner-ids`)
- `--seed-file` (default `.taskify-seed-state.json`)
- `--run` choices: `create`, `cleanup`, `create-and-cleanup`
- `--sample-size` (future enhancement; adjust counts)
- `--dry-run` (skips mutating calls, prints planned payloads)
- `--verbose`

### Logging & Error Handling
- Console output includes phase headers, counts, and warnings for skips/conflicts.
- Non-2xx responses logged with method, path, status, and truncated body.
- Script exits non-zero when fatal errors occur (e.g., missing prerequisites, repeated HTTP failures).
- Summary section printed at end with next-step hints (e.g., rerun diagnostics UI).

### Integration Points
- Reference this plan from README (Tooling section) once script lands.
- Encourage users to run diagnostics UI before/after to validate API health.
- Consider CI gating option later (e.g., nightly seeding check) using same script with `--base-url` pointing to test environment.
