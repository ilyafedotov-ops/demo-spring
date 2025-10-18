# Activity Log Guidelines

## Purpose
The activity log captures notable user-facing events that occur within Taskify. Entries fuel
timeline-style feeds, audit reports, and eventual webhook integrations. All producers should use the
shared `ActivityService` and `ActivityCodebook` constants to keep taxonomy, payload structure, and
naming consistent.

Each activity entry consists of:
- `entityType`: Uppercase identifier describing the aggregate that owns the activity (e.g. `TASK`).
- `entityId`: UUID of the entity the activity belongs to.
- `actorId`: UUID of the user responsible for the change. May be `null` for system actions.
- `action`: Uppercase verb representing what happened (e.g. `TASK_STATUS_CHANGED`).
- `payload`: JSON object with contextual fields needed for displaying the activity.
- `occurredAt`: Timestamp recorded in UTC (`Instant`).

## Taxonomy

| Entity Type | Description | Typical Source |
| --- | --- | --- |
| `TASK` | Task lifecycle changes (creation, status transitions, assignment updates, detail edits). | `TaskService` |
| `COMMENT` | Comment lifecycle events associated with a task. | `CommentService` |

### Actions & Payload Contracts

| Action | Payload Keys | Notes |
| --- | --- | --- |
| `TASK_CREATED` | `title` (String), `projectId` (UUID), `status` (String) | Fired when a task is created. `status` is the initial value. |
| `TASK_STATUS_CHANGED` | `from` (String), `to` (String) | Values map to `TaskStatus` enum names. |
| `TASK_ASSIGNED` | `from` (UUID\|null), `to` (UUID\|null) | `null` indicates unassigned. Include both keys even if unchanged. |
| `TASK_UPDATED` | `title` (String), `priority` (String), `dueDate` (String\|null) | `dueDate` uses ISO-8601 (yyyy-MM-dd) or `null` if cleared. |
| `TASK_TAGGED` | `tagId` (UUID), `tagName` (String), `tagColor` (String) | Fired after successfully attaching a tag to a task. `tagColor` stores the current hex value. |
| `TASK_UNTAGGED` | `tagId` (UUID), `tagName` (String), `tagColor` (String) | Emitted when a tag relationship is removed. Include `tagName` even if tag metadata changes later. |
| `COMMENT_ADDED` | `taskId` (UUID), `body` (String) | `body` holds the persisted comment text; trim before persisting. |
| `COMMENT_REMOVED` | `taskId` (UUID) | Emitted when a comment is deleted. |

## Producer Guidelines

1. **Always use `ActivityService.record`** instead of writing to the repository directly. This
   centralises validation and UUID/timestamp generation.
2. **Reference `ActivityCodebook`** constants for entity types and actions; avoid hard-coded strings.
3. **Keep payload keys deterministic**. Include keys for `from`/`to` semantics even when the value is
   `null`, so consumers can rely on shape. Prefer lowercase camel case for multi-word keys.
4. **Validate actors and entities upstream**. `ActivityService` assumes it receives correct IDs; ensure
   services assert entity existence before recording.
5. **Document new actions**. When adding new activity types, extend this file and update the iteration
   backlog with follow-up tasks (tests, API representation, UI implications).
6. **Avoid sensitive data**. Payloads must omit secrets or information restricted by role scope.

## Consumer Guidance

- Treat payload maps as immutable snapshots; do not expect side effects after emission.
- Use `occurredAt` ordering within a single entity. Cross-entity ordering should tolerate clock skew.
- Consumers should tolerate extra payload fields for forward compatibility but rely only on documented
  keys.

## Next Steps

- Evaluate asynchronous publication (Spring events or messaging) to decouple activity persistence from
  service transactions once the schema stabilises.

## Integration Checklist

- [ ] Confirm the service validates target entities/users before recording activity to avoid orphaned IDs.
- [ ] Use `ActivityCodebook` constants for both `entityType` and `action`; add new constants before
      introducing new activity kinds.
- [ ] Populate payload maps with deterministic keys and include `null` values where needed (e.g., `from`,
      `to`) to preserve shape. Convey tag metadata (`tagId`, `tagName`, `tagColor`) explicitly when
      recording tagging events.
- [ ] Add or update unit tests capturing the `ActivityService.record` arguments to verify payload contents.
- [ ] Update `docs/activity-log.md` when introducing new actions, including payload key definitions and
      sample notes for consumers.
