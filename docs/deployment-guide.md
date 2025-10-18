# Deployment Guide

This guide outlines the steps required to run Taskify locally, containerise the service, and prepare
it for higher environments.

## Prerequisites

- **JDK 21** (matching the enforced toolchain)
- **Maven Wrapper** (bundled as `./mvnw`)
- **Docker** 24+ (optional, required for container builds and the local Postgres stack)
- **GNU Make** (optional convenience when scripting)

## Local Environment

1. **Install Dependencies**
   ```bash
   ./mvnw -v     # verifies Maven Wrapper and JDK availability
   java -version # ensure JDK 21 is active
   ```

2. **Configure Environment Variables**
   - Copy `.env.sample` to `.env` if using Docker Compose.
   - Default application properties live in `src/main/resources/application.yml`.
   - Important variables:
     - `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`
     - `TASKIFY_DB_SCHEMA` (defaults to `public`)
     - `SERVER_PORT` (defaults to `8080`)

3. **Run Database (optional)**
   ```bash
   docker compose -f docker-compose.local.yml --env-file .env up -d
   ```
   This launches PostgreSQL 16 and pgAdmin with credentials sourced from `.env`. Flyway migrations
   run automatically when the Spring Boot app starts.

4. **Start the Application**
   ```bash
   ./mvnw spring-boot:run
   ```
   The service listens on `http://localhost:8080`. All API calls require the `X-Actor-Id` header
   containing a valid UUID.

5. **Execute Tests & Quality Gates**
   ```bash
   ./mvnw verify
   ```
   - Unit tests, integration tests (backed by Testcontainers), Spotless, SpotBugs, OWASP Dependency
     Check, and JaCoCo coverage run in the same lifecycle.
   - JaCoCo enforces a minimum 70% instruction coverage per class (selective exclusions apply) and
     produces HTML reports under `target/site/jacoco`.
   - SpotBugs runs with `Max` effort and `High` threshold, causing the build to fail on critical
     static-analysis findings.
   - OWASP Dependency Check stores its cache under `target/dependency-check-data` (configurable in
     `pom.xml`) to speed up repeat executions.

## Container Image

1. **Build**
   ```bash
   docker build -t taskify:latest .
   ```
   The multi-stage Dockerfile compiles the application and produces a slim Temurin 21 JRE runtime
   image. The image honours `JAVA_OPTS` and runs as a non-root `taskify` user.

2. **Run**
   ```bash
   docker run --rm -p 8080:8080 \
     -e SPRING_PROFILES_ACTIVE=local \
     -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/taskify \
     -e SPRING_DATASOURCE_USERNAME=taskify \
     -e SPRING_DATASOURCE_PASSWORD=taskify \
     taskify:latest
   ```
   Adjust datasource values according to your environment. For quick smoke tests without an external
   database you can rely on the in-memory profile (H2) by omitting datasource overrides, although
   PostgreSQL parity is recommended.

3. **Publishing**
   - The CI workflow (GitHub Actions) is configured to log in to GHCR on pushes to `main`, build the
     Docker image with Buildx caching, and push branch/SHA tags.

## Database Schema Management

- Flyway migrations live in `src/main/resources/db/migration`.
- Testcontainers automatically provisions PostgreSQL during integration tests, applying Flyway
  migrations before executing suites.
- For production environments, run Flyway via the application on startup or trigger Flyway CLI as
  part of your pipeline.

## Environment Configuration

| Variable | Purpose | Default |
| --- | --- | --- |
| `SPRING_PROFILES_ACTIVE` | Runtime profile (`local`, `test`, `prod`). | `prod` |
| `SPRING_DATASOURCE_URL` | JDBC connection string. | `jdbc:postgresql://localhost:5432/taskify` |
| `SPRING_DATASOURCE_USERNAME` | Database user. | `taskify` |
| `SPRING_DATASOURCE_PASSWORD` | Database password. | `taskify` |
| `TASKIFY_DB_SCHEMA` | Target schema for Flyway and Hibernate. | `public` |
| `SERVER_PORT` | HTTP port. | `8080` |
| `JAVA_OPTS` | Additional JVM flags in Docker image. | *(empty)* |

### Runtime Profiles

- **`local`**: Enables verbose SQL logging (configurable), lower connection pool sizes, and disables
  production-only integrations. Activate via `SPRING_PROFILES_ACTIVE=local`.
- **`test`**: Applied automatically during integration tests; disables Flyway auto-run and configures
  Testcontainers-friendly datasource overrides.
- **`prod`**: Default profile that expects externally managed datasource credentials and keeps security
  filters enabled for `/api/**`.

## Observability & Operations

- Spring Boot Actuator exposes `/actuator/health` and `/actuator/info` without authentication.
- Application logs use structured output; integrate with your log aggregation solution.
- Consider enabling metrics export (Micrometer) and log shipping based on your deployment platform.
  The default configuration already exposes `metrics` and `prometheus` endpoints once a registry is
  plugged in.

## Promotion Checklist

1. Provision PostgreSQL with the desired schema and credentials.
2. Populate the environment variables listed above.
3. Deploy the container image built from the matching Git SHA.
4. Run database migrations (Flyway) before switching traffic.
5. Configure ingress to add the `X-Actor-Id` header for initial testing or integrate proper JWT auth
   once implemented.
6. Monitor health endpoints and logs during rollout.

## CI/CD Overview

- The GitHub Actions workflow (`.github/workflows/ci.yml`) executes on pushes and pull requests:
  1. Checks out the repository and restores the OWASP dependency cache.
  2. Installs Temurin JDK 21 with Maven dependency caching via `actions/setup-java`.
  3. Runs `./mvnw -B verify`, enforcing tests, linting, coverage, SpotBugs, and dependency scanning.
  4. Generates the OpenAPI spec using the `openapi` Maven profile and fails if `docs/openapi.json`
     drifts from the generated output.
  5. Uploads the OpenAPI artifact for review (visible in the Actions tab).
  6. Builds the Docker image via Buildx; pushes to GHCR when the branch is `main`, leveraging cache
     imports/exports for faster builds.
- When introducing new deployment environments, reuse the image produced by CI to guarantee parity
  between staging and production.

## Secrets & Configuration Management

- Avoid committing environment-specific secrets. Use secret stores (GitHub Actions secrets, Vault,
  AWS/GCP secret managers, etc.) to inject runtime values securely.
- Supply credentials (database, messaging, third-party APIs) via environment variables or encrypted,
  mounted configuration files.
- Rotate secrets regularly and audit CI logs to ensure no sensitive values are echoed during builds.

## Scaling Considerations

- The application is stateless; run multiple replicas behind a load balancer. Ensure each pod/task
  sets `X-Actor-Id` headers appropriately for external clients until JWT integration is complete.
- Tune connection pool sizes via `taskify.datasource.pool` properties to match database capacity.
- Activity logging currently operates synchronously within request transactions; for extreme loads,
  consider introducing asynchronous event processing (Spring listeners, messaging queues).
- Monitor database indexes and query plans periodically—task tagging and activity feeds rely on the
  provided indexes for performance.
