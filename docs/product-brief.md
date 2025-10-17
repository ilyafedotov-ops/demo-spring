# Product Brief – Taskify Demo

## Vision
Deliver a Spring Boot–based Team Tasks management demo that illustrates production-ready patterns (DDD-inspired layering, security, observability, CI/CD) while remaining approachable for onboarding workshops and technical showcases.

## Personas
- **Team Member**: Captures and updates personal tasks, collaborates through comments, tracks due dates.
- **Team Lead**: Oversees project-level progress, assigns tasks, monitors activity feed for status changes.
- **Auditor**: Requires read-only visibility into projects and tasks for compliance or reporting.

## Core Use Cases
1. Create and manage projects with metadata (owner, status, start/end dates).
2. Create tasks, set priority/status, assign to team members, and track lifecycle transitions.
3. Comment on tasks to clarify requirements and capture discussions.
4. Apply tags to tasks for filtering/reporting and retrieve tag catalog.
5. Review activity feed capturing key domain events (task status changes, comments, assignments).
6. Authenticate via JWT, manage personal profile, and respect role-based access rules.

## Boundaries & Non-Goals
- Not building a full UI; REST/OpenAPI exposure only.
- No real-time notifications or external integrations (e.g., Slack) in scope.
- No reporting dashboards; deliver APIs enabling analytics instead.

## Non-Functional Requirements
- Clean module boundaries (web, application, domain, infrastructure packages).
- Security best practices (password hashing, JWT, role-based authorization, auditing).
- Container-friendly build with layered jars and fast local onboarding via Docker Compose.
- Observability via Spring Boot Actuator, Micrometer Prometheus metrics, structured logging.
- Automated testing coverage target ≥80% across unit/slice/integration suites.
- CI/CD pipeline demonstrating build → test → scan → container publish.

## Open Questions
- Should demo include multi-tenancy support or single-tenant only?
- Preferred identity provider integration for JWT issuance (local only vs. external IdP)?
- Desired demo hosting environment (local Docker vs. cloud deployment example)?
- Are seed datasets required to showcase typical workflows?

## Key Deliverables
- Spring Boot backend with documented API and seed data.
- Infrastructure-as-code artifacts (Docker Compose, K8s manifests) for deployment scenarios.
- Developer documentation (README, ADRs, architecture diagram, onboarding guide).
- CI workflow definitions and quality gates aligned with success metrics.
