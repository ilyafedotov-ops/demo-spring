# Project Status — 2025-10-18

## Completed
- Core domain aggregates with transition rules and domain events (Milestone 3 baseline).
- Persistence adapters for tasks, projects, users, tags, comments, and activity log with Testcontainers-backed integration tests.
- Application services covering task orchestration, tag management, comment lifecycle, and activity logging, including initial ActivityService hooks for task/comment actions.
- Activity logging guidelines anchored in `docs/activity-log.md`, now linked from README with integration checklist for adopters (closes IB-05 documentation work).
- IB-01: Activity payloads now normalise null-safe, deterministic keys within TaskService and CommentService; accompanying unit tests assert recorded payload structures.
- IB-03: Service tests now exercise assignment unassign flows and null-actor comment removal, confirming activity payload shape coverage.
- IB-02: TaskService now exposes tag attach/detach operations backed by a task-tag repository adapter and emits deterministic activity payloads for tagging events.
- IB-04: `CommentService.deleteComment` now performs single-repository deletes, returns an outcome flag, and records activity payloads without redundant lookups.
- API: `TaskController` exposes `/api/tasks/{taskId}/tags/{tagId}` attach/detach endpoints consuming `X-Actor-Id` headers and translating service errors into HTTP 404 responses.
- Persistence: `TaskTagRepositoryAdapter` integration tests verify Postgres join table operations, guarding against duplicate attachments and confirming detach semantics.
- Security: Lightweight header-based authentication filter converts `X-Actor-Id` into the Spring Security context for all `/api/**` routes; controller logic now resolves the actor from the authenticated principal.
- Integration: Full-stack `TaskControllerIntegrationTest` exercises attach/detach endpoints against Postgres, asserting both tag relationships and activity log entries are persisted.
- Integration: `TaskServiceIntegrationTest` covers task lifecycle operations end-to-end, ensuring activity log entries capture status, assignment, and detail updates.
- Web/API: `TaskController` now serves create, detail fetch, project-scoped listing, assignment, status update, and tagging endpoints using validated DTOs and a MapStruct-backed `TaskMapper`; WebMvc + Testcontainers integration suites cover happy-path, validation, and conflict responses.
- Web/API: Introduced `ProjectController`, `TagController`, `CommentController`, `UserController`, and `ActivityController` with dedicated DTOs/mappers, centralized error handling, and WebMvc suites validating create/list/update/delete and feed queries; OpenAPI spec exported to `docs/openapi.json` and validated in CI.
- Testing: Added unit coverage for `ActorResolver`, query services, `SecurityAuditorAware`, `UserRepositoryAdapter`, and supporting JPA helpers; exercised MapStruct mappers directly and tuned JaCoCo rules so the 70% class-level gate now passes with generated implementations excluded and the Spring Boot launcher whitelisted.
- DevOps: Multi-stage Dockerfile builds a slim Temurin 21 runtime image with a dedicated non-root user and honours `JAVA_OPTS`; custom `BOOT-INF/layers.idx` groups dependencies, static assets, configuration, and application classes for cache-friendly deployments.
- CI: GitHub Actions now runs the full Maven `verify`, generates OpenAPI artefacts, and builds the Docker image with Buildx cache reuse; merges to `main` push tags to GHCR (`ghcr.io/<repo>`).

## In Progress
- Milestone 4: Layer in shared response envelopes/OpenAPI documentation and extend coverage to remaining domain modules (e.g., user mutations, task pagination).
- Milestone 8 follow-up: Wire in dependency/security scanners and static analysis gates ahead of delivery readiness.

## Upcoming
- Wire ActivityService usage checks into controller-level integration tests once more endpoints surface (IB-03 follow-up).
- Add OpenAPI DTO exposure and mapper tests once remaining controllers come online (Milestone 4.2/4.3).
- Document the new controller endpoints (reference tables, sample requests) and thread the error-handling conventions into the onboarding guide.

## Notes
- `docs/activity-log.md` now documents tagging actions (`TASK_TAGGED`, `TASK_UNTAGGED`) alongside payload requirements; ensure downstream consumers align with the new fields.
- Controller endpoints currently rely on caller-provided `X-Actor-Id` processed by the new security filter; replace with proper auth tokens once the security milestone kicks off.
- ActivityService remains synchronous; evaluate wrapping via application events once payload schema stabilises.
- Integration suites spin up Postgres/Flyway via Testcontainers; expect ~2s start time per suite during local runs.
- Keep an eye on Mockito's inline mock warnings; the current self-attaching agent remains compatible, but future JDK releases may require passing the agent explicitly via build tooling.
