# API Reference

All endpoints live under the `/api` prefix and require the `X-Actor-Id` header containing a valid
UUID. Responses use JSON and errors conform to RFC‑7807 `ProblemDetail` representations.

## Authentication & Headers

- **Required header**: `X-Actor-Id: <uuid>`
- The header value must be a valid UUID representing the acting user. The identifier is propagated to
  application services for auditing, ownership checks, and activity logging.
- Requests missing the header or providing an invalid UUID receive `401 Unauthorized`.
- For write operations, the acting user is assumed to be the creator/updater unless the API explicitly
  allows overriding (e.g., assigning tasks to another user).

### Example Request

```http
POST /api/tasks HTTP/1.1
Host: localhost:8080
Content-Type: application/json
X-Actor-Id: 9e5f5c2d-843d-4d08-9b9f-5f2c8181f7e2

{
  "projectId": "b5f5984f-09b0-4c3f-8a64-3bcbf325d9a3",
  "title": "Draft project plan",
  "description": "Outline key milestones and owners",
  "priority": "HIGH",
  "status": "TODO",
  "assigneeId": null,
  "dueDate": "2025-01-31"
}
```

### Example Success Response

```http
HTTP/1.1 201 Created
Location: /api/tasks/e2a6b5d8-7a15-418a-8a0f-d892db2b97f8
Content-Type: application/json

{
  "id": "e2a6b5d8-7a15-418a-8a0f-d892db2b97f8",
  "projectId": "b5f5984f-09b0-4c3f-8a64-3bcbf325d9a3",
  "title": "Draft project plan",
  "description": "Outline key milestones and owners",
  "priority": "HIGH",
  "status": "TODO",
  "assigneeId": null,
  "dueDate": "2025-01-31",
  "completedAt": null,
  "createdBy": "9e5f5c2d-843d-4d08-9b9f-5f2c8181f7e2",
  "createdAt": "2025-01-05T10:15:30.000Z",
  "updatedBy": "9e5f5c2d-843d-4d08-9b9f-5f2c8181f7e2",
  "updatedAt": "2025-01-05T10:15:30.000Z"
}
```

### Example Error Response

```http
HTTP/1.1 400 Bad Request
Content-Type: application/problem+json

{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "taskId must not be null"
}
```

## Tasks (`/api/tasks`)

| Method & Path | Description | Request Body | Success Response |
| --- | --- | --- | --- |
| `POST /api/tasks` | Create a new task within a project. | `TaskCreateRequest` (title, projectId, optional description/priority/status/assignee/dueDate). | `201 Created` with `TaskResponse`; `Location` header points to the new task. |
| `GET /api/tasks/{taskId}` | Fetch a task by ID. | – | `200 OK` with `TaskResponse`. |
| `GET /api/tasks?projectId=<uuid>` | List tasks in a project (ordered by creation descending). | – | `200 OK` array of `TaskResponse`. |
| `PUT /api/tasks/{taskId}` | Update core task details (title, description, priority, due date). | `TaskUpdateDetailsRequest`. | `200 OK` with updated `TaskResponse`. |
| `PATCH /api/tasks/{taskId}/assignee` | Assign or unassign a task. | `TaskAssignRequest` (assigneeId nullable). | `200 OK` with `TaskResponse`. |
| `PATCH /api/tasks/{taskId}/status` | Transition task status following domain rules. | `TaskStatusUpdateRequest`. | `200 OK` with `TaskResponse`. |
| `POST /api/tasks/{taskId}/tags/{tagId}` | Attach an existing tag to the task. Duplicate attachments are ignored. | – | `204 No Content`. |
| `DELETE /api/tasks/{taskId}/tags/{tagId}` | Remove a tag from the task. No-op if the tag is not attached. | – | `204 No Content`. |

**Notes**
- `TaskStatus` accepts `TODO`, `IN_PROGRESS`, `BLOCKED`, `DONE`, `CANCELLED`. Transitions follow the
  guard rules defined in `TaskStatus#canTransitionTo` (e.g., `DONE → CANCELLED` allowed, `DONE → TODO`
  rejected).
- `TaskPriority` accepts `LOW`, `MEDIUM`, `HIGH`.
- `assigneeId` must be `null` or reference an existing user; otherwise the service returns
  `400 Bad Request` with `detail: "Assignee does not exist"`.

## Projects (`/api/projects`)

| Method & Path | Description | Request Body | Success Response |
| --- | --- | --- | --- |
| `POST /api/projects` | Create a project owned by a user. | `ProjectCreateRequest`. | `201 Created` with `ProjectResponse`. |
| `GET /api/projects/{projectId}` | Retrieve project details. | – | `200 OK` with `ProjectResponse`. |
| `GET /api/projects` | List all projects (unordered). | – | `200 OK` array of `ProjectResponse`. |
| `PUT /api/projects/{projectId}` | Update project name/description/schedule. | `ProjectUpdateRequest`. | `200 OK` with `ProjectResponse`. |
| `PATCH /api/projects/{projectId}/status` | Change project status (guards invalid transitions). | `ProjectStatusUpdateRequest`. | `200 OK` with `ProjectResponse`. |
| `PATCH /api/projects/{projectId}/owner` | Reassign project owner (verifies user existence). | `ProjectOwnerChangeRequest`. | `200 OK` with `ProjectResponse`. |

**Notes**
- `ProjectStatus` accepts `PLANNED`, `ACTIVE`, `ON_HOLD`, `COMPLETED`, `CANCELLED`. Invalid
  transitions trigger `409 Conflict` with descriptive messages.
- `ownerId` must refer to an existing user.

## Comments (`/api/tasks/{taskId}/comments`)

| Method & Path | Description | Request Body | Success Response |
| --- | --- | --- | --- |
| `POST /api/tasks/{taskId}/comments` | Add a comment authored by the acting user. | `CommentCreateRequest` (body). | `201 Created` with `CommentResponse`. |
| `GET /api/tasks/{taskId}/comments` | List all comments for a task (ascending by creation). | – | `200 OK` array of `CommentResponse`. |
| `DELETE /api/tasks/{taskId}/comments/{commentId}` | Delete a comment. | – | `204 No Content` or `404` if already removed. |

## Tags (`/api/tags`)

| Method & Path | Description | Request Body | Success Response |
| --- | --- | --- | --- |
| `POST /api/tags` | Create a new tag (unique name). | `TagCreateRequest`. | `201 Created` with `TagResponse`. |
| `GET /api/tags` | List all tags. | – | `200 OK` array of `TagResponse`. |

## Users (`/api/users`)

| Method & Path | Description | Request Body | Success Response |
| --- | --- | --- | --- |
| `GET /api/users` | Retrieve all users (directory view). | – | `200 OK` array of `UserResponse`. |
| `GET /api/users/{userId}` | Fetch a user by ID. | – | `200 OK` with `UserResponse`. |

## Activity (`/api/activity`)

| Method & Path | Description | Query Parameters | Success Response |
| --- | --- | --- | --- |
| `GET /api/activity` | Retrieve activity feed entries. | `entityType` (required), `entityId` (optional), `limit` (default 20). | `200 OK` array of `ActivityResponse`. When `entityId` is provided, results are scoped to that entity; otherwise recent entries for the type are returned. |

**Notes**
- Supported `entityType` values align with `ActivityCodebook` (`TASK`, `COMMENT`). Additional types
  can be introduced as the domain expands.
- `limit` must be positive; invalid values yield `400 Bad Request`.
- Activity payloads are deterministic maps documented in `docs/activity-log.md`.

## Error Handling

- Validation failures and illegal arguments: `400 Bad Request` (ProblemDetail).
- Missing resources: `404 Not Found` (ProblemDetail).
- Conflict scenarios (invalid status transitions, duplicate actions): `409 Conflict`.
- Missing `X-Actor-Id` header: `401 Unauthorized`.
- Server-side errors generate `500 Internal Server Error` with a generic `ProblemDetail`. Logs should
  be inspected for stack traces and diagnostics.

## Data Types & Formatting

- **UUIDs**: All identifiers (tasks, projects, users, tags, activity entries) use standard
  hyphen-separated UUID strings.
- **Timestamps**: ISO-8601 instant strings (`UTC`) via `Instant.toString()`.
- **Dates**: `yyyy-MM-dd` (e.g., due dates, project schedules).
- **Enums**: Sent as uppercase names (`TaskStatus`, `TaskPriority`, `ProjectStatus`, `UserRole`,
  `UserStatus`).

## Pagination & Filtering

- The initial implementation provides project-scoped task listing and activity feed limiting.
- Additional filtering/pagination extensions should be documented in this guide and the OpenAPI spec
  as they are introduced.

## Versioning & Documentation

- The generated OpenAPI document is published to `docs/openapi.json` and validated in CI.
- `docs/openapi.html` embeds Swagger UI for quick browsing of the local specification.
- Backwards-incompatible API changes should increment the documented version in `OpenApiConfig` and
  update client integrations accordingly.
