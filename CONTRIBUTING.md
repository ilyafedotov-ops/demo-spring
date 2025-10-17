# Contributing to Taskify

We welcome contributions that improve the demo or showcase additional Spring Boot best practices. Please follow the guidelines below to keep the repository consistent and healthy.

## Getting Started
- Ensure you have JDK 21 available (`JAVA_HOME` should point to it when running Maven). The repository README describes how to download a local JDK if necessary.
- Install Docker if you plan to run the Postgres development stack.
- Run `./mvnw spotless:apply` once to normalize formatting before committing your first change.

## Workflow
1. Create a feature branch from `main`.
2. Keep commits focused and descriptive; prefer the Conventional Commits style when possible.
3. Before submitting a pull request:
   - Run `./mvnw verify`.
   - Run any relevant Testcontainers or integration tests if you touched persistence.
   - Update docs/ADRs when architectural decisions change.
4. Submit a PR that references any related issue and includes a concise summary of the change, testing evidence, and screenshots if applicable.

## Code Style & Tooling
- Java code is formatted with Spotless + Google Java Format. The CI pipeline will fail if formatting is off.
- Enable the provided pre-commit hook (`scripts/git-hooks/pre-commit`) or wire it into your preferred hook runner to catch formatting/test failures locally.
- For configuration and documentation changes, keep lines wrapped at ~100 characters and prefer Markdown tables or lists for clarity.

## Reporting Issues
- Use GitHub Issues to report bugs or request enhancements.
- Provide reproduction steps, expected vs. actual behavior, and environment details (OS, JDK version, Maven version).

## Security
- Do not include secrets in the repository. Use environment variables or secret managers.
- Report vulnerabilities privately to the maintainers before creating a public issue.

Thanks for helping make Taskify a solid reference project! 😊
