# API Reference

All endpoints live under the `/api` prefix and require the `X-Actor-Id` header containing a valid
UUID. Responses use JSON and errors conform to RFC‑7807 `ProblemDetail` representations.

## Authentication

- **Header**: `X-Actor-Id: <uuid>`
- Requests missing the header or providing an invalid UUID receive `401 Unauthorized`.

## Tasks (`/api/tasks`)

| Method & Path | Description | Request Body | Success Response |
| --- | --- | --- | --- |
| `POST /api/tasks` | Create a new task within a project. | `TaskCreateRequest` (title, projectId, optional description/priority/status/assignee/dueDate). | `201 Created` with `TaskResponse`; `Location` header points to the new task. |
| `GET /api/tasks/{taskId}` | Fetch a task by ID. | – | `200 OK` with `TaskResponse`. |
| `GET /api/tasks?projectId=<uuid>` | List tasks in a project (ordered by creation desc). | – | `200 OK` array of `TaskResponse`. |
| `PUT /api/tasks/{taskId}` | Update core task details (title, description, priority, due date). | `TaskUpdateDetailsRequest`. | `200 OK` with updated `TaskResponse`. |
| `PATCH /api/tasks/{taskId}/assignee` | Assign or unassign a task. | `TaskAssignRequest` (assigneeId nullable). | `200 OK` with `TaskResponse`. |
| `PATCH /api/tasks/{taskId}/status` | Transition task status following domain rules. | `TaskStatusUpdateRequest`. | `200 OK` with `TaskResponse`. |
| `POST /api/tasks/{taskId}/tags/{tagId}` | Attach a tag to the task. | – | `204 No Content`. |
| `DELETE /api/tasks/{taskId}/tags/{tagId}` | Remove a tag from the task. | – | `204 No Content`. |

## Projects (`/api/projects`)

| Method & Path | Description | Request Body | Success Response |
| --- | --- | --- | --- |
| `POST /api/projects` | Create a project owned by a user. | `ProjectCreateRequest`. | `201 Created` with `ProjectResponse`. |
| `GET /api/projects/{projectId}` | Retrieve project details. | – | `200 OK` with `ProjectResponse`. |
| `GET /api/projects` | List all projects. | – | `200 OK` array of `ProjectResponse`. |
| `PUT /api/projects/{projectId}` | Update project name/description/schedule. | `ProjectUpdateRequest`. | `200 OK` with `ProjectResponse`. |
| `PATCH /api/projects/{projectId}/status` | Change project status (with transition checks). | `ProjectStatusUpdateRequest`. | `200 OK` with `ProjectResponse`. |
| `PATCH /api/projects/{projectId}/owner` | Reassign project owner. | `ProjectOwnerChangeRequest`. | `200 OK` with `ProjectResponse`. |

## Comments (`/api/tasks/{taskId}/comments`)

| Method & Path | Description | Request Body | Success Response |
| --- | --- | --- | --- |
| `POST /api/tasks/{taskId}/comments` | Add a comment authored by the acting user. | `CommentCreateRequest` (body). | `201 Created` with `CommentResponse`. |
| `GET /api/tasks/{taskId}/comments` | List all comments for a task. | – | `200 OK` array of `CommentResponse`. |
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

## Error Handling

- Validation failures and illegal arguments: `400 Bad Request` (ProblemDetail).
- Missing resources: `404 Not Found` (ProblemDetail).
- Conflict scenarios (invalid status transitions, duplicate actions): `409 Conflict`.
- Missing `X-Actor-Id` header: `401 Unauthorized`.

## Versioning & Documentation

- The generated OpenAPI document is published to `docs/openapi.json` and validated in CI.
- `docs/openapi.html` embeds Swagger UI for quick browsing of the local specification.

