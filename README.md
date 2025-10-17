# Taskify

Taskify is a Spring Boot 3 demo application that showcases production-ready patterns for a team task management backend: layered architecture, security, observability, CI/CD, and documentation. It powers personas such as team members, team leads, and auditors with APIs for projects, tasks, comments, tags, and activity tracking.

## Architecture Snapshot
- Java 21, Spring Boot 3.5.x, layered per feature (`web`, `application`, `domain`, `infrastructure`).
- Persistence with PostgreSQL (Flyway migrations, Spring Data JPA).
- Security via Spring Security JWT (planned).
- Observability via Actuator, Micrometer, structured logging.
- API documentation powered by springdoc OpenAPI.

See `docs/project-plan.md` and the ADRs in `docs/adr/` for the detailed roadmap and decisions.

## Prerequisites
- **JDK 21** – Install locally or use the helper tarball download steps below.
- **Maven Wrapper** – Provided via `./mvnw`, no global Maven install required.
- **Docker** (optional for now) – Needed once the local Postgres stack is introduced.

### Quick JDK 21 Setup
If you do not have a full JDK available, download one into the repository:

```bash
mkdir -p tools
curl -sSL -o tools/jdk21.tar.gz https://download.oracle.com/java/21/latest/jdk-21_linux-x64_bin.tar.gz
tar -xzf tools/jdk21.tar.gz -C tools
export JAVA_HOME="$(pwd)/tools/jdk-21.0.8"
export PATH="$JAVA_HOME/bin:$PATH"
```

Adjust the extracted directory name if a newer patch release is downloaded.

## Getting Started
```bash
# Format code (optional, but recommended before the first commit)
JAVA_HOME="$(pwd)/tools/jdk-21.0.8" ./mvnw spotless:apply

# Run the verification suite (includes tests, coverage, formatting check)
JAVA_HOME="$(pwd)/tools/jdk-21.0.8" ./mvnw verify
```

## Local Infrastructure
- Copy `.env.sample` to `.env` and adjust credentials if necessary.
- Start the local Postgres + pgAdmin stack:

```bash
docker compose -f docker-compose.local.yml --env-file .env up -d
```

- Access pgAdmin at `http://localhost:5050` using the credentials defined in `.env`.
- Apply Flyway migrations automatically by starting the Spring Boot application (`./mvnw spring-boot:run`) or running `./mvnw flyway:migrate` once database credentials are reachable.

## Development Workflow
1. Create a feature branch from `main`.
2. Implement the change, updating docs/ADRs when architecture decisions shift.
3. Run `./mvnw verify` (with `JAVA_HOME` pointing to your JDK 21 if necessary).
4. Open a pull request describing the change and test evidence.

### Git Hooks
A helper hook is provided under `scripts/git-hooks/pre-commit`. Link it into your local `.git/hooks` directory or integrate with your preferred hook manager so that formatting and tests run before each commit.

```bash
ln -s ../../scripts/git-hooks/pre-commit .git/hooks/pre-commit
chmod +x scripts/git-hooks/pre-commit
```

## Formatting & Quality Gates
- Spotless + Google Java Format enforce style (`./mvnw spotless:apply` to fix).
- JaCoCo coverage reports are generated during `./mvnw verify`.
- Additional linters and security scanners will be wired into CI (see `docs/project-plan.md` Milestone 8).

## Configuration
- Configuration profiles will live in `src/main/resources/application*.yml`.
- Secret material belongs in environment variables. A `.env.sample` file will document required variables.

## Documentation
- **Project Plan** – `docs/project-plan.md`
- **Product Brief** – `docs/product-brief.md`
- **Glossary** – `docs/glossary.md`
- **Architecture Decision Records** – `docs/adr/`

Contributions are welcome—see `CONTRIBUTING.md` for guidance.
