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
   - JaCoCo enforces a minimum 70% instruction coverage per class (selective exclusions apply).

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

## Observability & Operations

- Spring Boot Actuator exposes `/actuator/health` and `/actuator/info` without authentication.
- Application logs use structured output; integrate with your log aggregation solution.
- Consider enabling metrics export (Micrometer) and log shipping based on your deployment platform.

## Promotion Checklist

1. Provision PostgreSQL with the desired schema and credentials.
2. Populate the environment variables listed above.
3. Deploy the container image built from the matching Git SHA.
4. Run database migrations (Flyway) before switching traffic.
5. Configure ingress to add the `X-Actor-Id` header for initial testing or integrate proper JWT auth
   once implemented.
6. Monitor health endpoints and logs during rollout.

