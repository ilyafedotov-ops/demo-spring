# Glossary

- **Activity Feed**: Stream of notable events (task status change, comment added, assignment updates) persisted for auditing.
- **Auditor**: Read-only persona responsible for reviewing work items without modifying data.
- **JWT (JSON Web Token)**: Token format used for stateless authentication containing user identity and roles.
- **Project**: Top-level container grouping tasks aligned to a defined objective, with owner and lifecycle status.
- **Task**: Work item within a project carrying assignee, priority, due date, and status transitions.
- **Tag**: Descriptive label applied to tasks to support filtering and categorization.
- **Team Lead**: Persona managing projects, assigning tasks, and monitoring progress; holds elevated permissions.
- **Team Member**: Persona managing personal tasks, updating status, and participating in collaboration.
- **Activity Log**: Persistence table storing structured event data powering the activity feed.
- **Status Transition**: Domain rule controlling allowed state changes for tasks or projects.
