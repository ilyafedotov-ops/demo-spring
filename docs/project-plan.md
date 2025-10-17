# Demo Spring Boot App – Project Plan

## Milestone 0 – Alignment
- Task 0.1: Schedule and run kickoff workshop with stakeholders to confirm personas, core flows, and must-have features.
- Task 0.2: Document refined scope in `docs/product-brief.md`; capture open questions and owners.
- Task 0.3: Draft success metrics (API coverage target, test thresholds, deployment expectations) and review with tech lead.
- Task 0.4: Register ADR-000 describing project goals, constraints, and success metrics; circulate for sign-off.
- Task 0.5: Create shared glossary of domain terms (entities, statuses, role names) in `docs/glossary.md`; align with product wording.

## Milestone 1 – Project Scaffolding & Tooling
- Task 1.1: Use Spring Initializr CLI to generate project skeleton with package `com.example.taskify` and required dependencies.
- Task 1.2: Commit generated wrapper scripts (`mvnw`, `mvnw.cmd`) and verify build succeeds locally (`./mvnw verify`).
- Task 1.3: Configure Maven plugins (Spring Boot, MapStruct annotation processor, JaCoCo, Spotless) in `pom.xml`.
- Task 1.4: Pin toolchain to JDK 21 using Maven Toolchains plugin and update `README` with prerequisite instructions.
- Task 1.5: Add repository hygiene files (LICENSE, CONTRIBUTING.md, CODEOWNERS, `.editorconfig`, `.gitignore`, `Renovate.json`).
- Task 1.6: Install Husky or Lefthook pre-commit hooks running Spotless, unit tests, and lint checks on staged changes.

## Milestone 2 – Infrastructure Baseline
- Task 2.1: Write `docker-compose.local.yml` for Postgres + pgAdmin; document usage in README.
- Task 2.2: Create `.env.sample` holding database credentials and JWT secrets; ensure `.env` is gitignored.
- Task 2.3: Define base `application.yml` with common properties and dedicated profile files (`application-local.yml`, `application-test.yml`).
- Task 2.4: Implement `DataSourceProperties` binding class and verify profile overrides from environment variables.
- Task 2.5: Author Flyway V1 migration containing schema for users, projects, tasks, comments, tags, task_tags, activity_log.
- Task 2.6: Configure JPA naming strategy, UUID generation helpers, and auditing beans in dedicated `@Configuration` class.

## Milestone 3 – Domain & Application Layer
- Task 3.1: Define enums for task status, priority, user roles, project status with documented transitions.
- Task 3.2: Implement JPA entities (`UserEntity`, `ProjectEntity`, `TaskEntity`, `CommentEntity`, `TagEntity`, `ActivityLogEntity`) with auditing columns.
- Task 3.3: Model domain aggregates and value objects in `domain` package, including invariants and guard clauses.
- Task 3.4: Create application services for task assignment, status changes, and comment posting with transactional boundaries.
- Task 3.5: Write unit tests covering domain invariants and service rules using JUnit + AssertJ.
- Task 3.6: Implement Spring Data repositories and query specifications for filtering tasks by status, assignee, and tags.
- Task 3.7: Define domain events (e.g., `TaskStatusChangedEvent`, `CommentAddedEvent`) and publish from application services.
- Task 3.8: Implement transactional event listeners persisting `ActivityLogEntity` records.

## Milestone 4 – Web & API Layer
- Task 4.1: Define request/response DTOs for users, projects, tasks, comments, tags, and activity items.
- Task 4.2: Configure MapStruct mappers between entities, domain models, and DTOs; add mapper tests.
- Task 4.3: Implement REST controllers with CRUD endpoints, pagination, and filtering support per resource.
- Task 4.4: Add method-level security annotations (`@PreAuthorize`, ownership checks) on controller methods.
- Task 4.5: Create global `@ControllerAdvice` handling validation errors, domain exceptions, and security exceptions with ProblemDetail.
- Task 4.6: Integrate springdoc OpenAPI starter, customize title/description, and add JWT security scheme.
- Task 4.7: Generate example requests/responses and ensure OpenAPI UI available under `/swagger-ui`.

## Milestone 5 – Security & Identity
- Task 5.1: Configure password encoder (BCrypt) and user details service backed by database.
- Task 5.2: Implement bootstrap data loader creating admin/lead demo accounts via CommandLineRunner.
- Task 5.3: Build authentication endpoint issuing JWT access and refresh tokens; include expiry and scopes claims.
- Task 5.4: Add JWT filter to security chain validating tokens and populating SecurityContext.
- Task 5.5: Configure authorization rules per endpoint (role hierarchy, ownership checks) and document in ADR.
- Task 5.6: Write integration tests covering login, token refresh, and protected endpoint access scenarios.
- Task 5.7: Implement `AuditorAware` bean to track authenticated user IDs on audited entities.
- Task 5.8: Persist security events (login success/failure) into `ActivityLogEntity` via event listeners.

## Milestone 6 – Cross-Cutting Concerns
- Task 6.1: Configure Caffeine caches for tags and reference data with TTL/size limits in `CacheConfig`.
- Task 6.2: Expose cache metrics and integrate with Micrometer registry.
- Task 6.3: Implement servlet filter injecting correlation IDs into MDC and responses.
- Task 6.4: Customize Logback pattern for structured JSON logging; document log fields.
- Task 6.5: Enable Actuator endpoints (health, info, metrics, prometheus) with role-based access control.
- Task 6.6: Add Micrometer Prometheus registry and configure scrape endpoint in README.
- Task 6.7: Set up `TaskExecutor` bean for asynchronous event handling and ensure activity logging uses async listeners.

## Milestone 7 – Testing Strategy
- Task 7.1: Create testing base classes and utility builders for domain entities and DTOs.
- Task 7.2: Configure Mockito and AssertJ dependencies; set global test configuration (JUnit 5 extensions).
- Task 7.3: Implement unit tests for domain services, security services, and utility classes with coverage >80%.
- Task 7.4: Author `@DataJpaTest` cases for repositories verifying queries and migrations.
- Task 7.5: Add `@WebMvcTest` slices for controllers to validate validation rules and error responses.
- Task 7.6: Integrate Testcontainers Postgres into integration tests; ensure Flyway migrations run on startup.
- Task 7.7: Implement end-to-end API tests with MockMvc/RestAssured covering auth flows and task lifecycle.
- Task 7.8: Publish coverage report artifacts in CI pipeline and fail build under threshold.

## Milestone 8 – DevOps & Delivery
- Task 8.1: Build multi-stage Dockerfile using Spring Boot layered jar; validate image locally.
- Task 8.2: Configure `layers.idx` customization if needed and document image layering strategy.
- Task 8.3: Author GitHub Actions workflow covering linting, unit tests, integration tests, and Docker build push.
- Task 8.4: Enable caching in CI (Maven, Docker layers) to speed up pipelines.
- Task 8.5: Integrate dependency scanning (OWASP Dependency Check/Snyk) and publish reports in CI.
- Task 8.6: Configure static code analysis (SonarCloud or CodeQL) with gates aligned to success metrics.
- Task 8.7: Provide deployment manifests (Kubernetes deployment/service, Docker Compose prod override) under `deploy/`.
- Task 8.8: Write operational runbook covering configuration, scaling, health checks, and log/metrics access.

## Milestone 9 – Documentation & Handoff
- Task 9.1: Update README with architecture overview diagram, setup instructions, troubleshooting tips, and sample API calls.
- Task 9.2: Complete ADRs for authentication strategy, DDD layering, database selection, and logging approach.
- Task 9.3: Generate OpenAPI JSON/YAML and publish via Redoc or Stoplight docs; include Postman collection export.
- Task 9.4: Prepare onboarding guide describing local environment setup, coding standards, and workflow.
- Task 9.5: Run final QA checklist covering security, performance sanity, docs completeness, and address remaining issues.
- Task 9.6: Conduct handoff/demo session capturing feedback and listing follow-up backlog items.
