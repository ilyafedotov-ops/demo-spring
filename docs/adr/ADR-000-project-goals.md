# ADR-000: Project Goals and Success Metrics

## Status
Proposed

## Context
We are creating a Spring Boot demo application that demonstrates production-grade patterns while remaining lightweight for workshops. Alignment on goals and measurable success criteria ensures the team builds features that showcase best practices without over-engineering.

## Decision
- Focus on a Team Tasks backend illustrating clean architecture, security, observability, and CI/CD.
- Measure success using the following criteria:
  - Deliver core API endpoints for projects, tasks, comments, tags, and activity feed that pass functional QA.
  - Achieve ≥80% line coverage across unit and integration tests, with key business rules covered by tests.
  - Provide automated CI pipeline (build, tests, static analysis, dependency scanning) with <10 min runtime.
  - Supply container image built via layered jar with cold start (from checkout) to running API ≤5 minutes on standard laptop.
  - Include operational telemetry (Actuator health/readiness, Prometheus metrics, structured logs) verified via smoke test scripts.
  - Document onboarding steps allowing new developer to run API locally in ≤30 minutes.
- Capture open questions (multi-tenancy expectations, identity provider integration, hosting target) for stakeholder follow-up.

## Consequences
- Engineering tasks should prioritize features that demonstrate the success metrics; non-essential capabilities are deferred.
- Testing and CI configuration must be planned early to ensure coverage and runtime goals are met.
- Documentation deliverables (README, onboarding guide) are tracked as first-class backlog items.
- Stakeholder check-ins must revisit outstanding open questions before implementation reaches affected areas (e.g., auth integration).
