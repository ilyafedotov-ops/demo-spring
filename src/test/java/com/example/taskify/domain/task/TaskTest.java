package com.example.taskify.domain.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TaskTest {

  private static final Instant NOW = Instant.parse("2025-01-01T10:15:30Z");
  private static final UUID TASK_ID = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
  private static final UUID PROJECT_ID = UUID.fromString("11111111-2222-3333-4444-555555555555");
  private static final UUID CREATOR_ID = UUID.fromString("99999999-8888-7777-6666-555555555555");

  @Test
  void createInitialisesDefaults() {
    Task task =
        Task.create(
            TASK_ID,
            PROJECT_ID,
            "  Build API ",
            null,
            TaskPriority.HIGH,
            null,
            null,
            LocalDate.of(2025, 2, 1),
            CREATOR_ID,
            () -> NOW);

    assertThat(task.getId()).isEqualTo(TASK_ID);
    assertThat(task.getProjectId()).isEqualTo(PROJECT_ID);
    assertThat(task.getTitle()).isEqualTo("Build API");
    assertThat(task.getPriority()).isEqualTo(TaskPriority.HIGH);
    assertThat(task.getStatus()).isEqualTo(TaskStatus.TODO);
    assertThat(task.getAssigneeId()).isEmpty();
    assertThat(task.getDueDate()).contains(LocalDate.of(2025, 2, 1));
    assertThat(task.getCompletedAt()).isEmpty();
    assertThat(task.getCreatedBy()).isEqualTo(CREATOR_ID);
    assertThat(task.getCreatedAt()).isEqualTo(NOW);
    assertThat(task.getUpdatedAt()).isEqualTo(NOW);
  }

  @Test
  void changeStatusRecordsEventAndUpdatesAudit() {
    Task task =
        Task.create(
            TASK_ID,
            PROJECT_ID,
            "Build API",
            null,
            TaskPriority.HIGH,
            TaskStatus.TODO,
            null,
            null,
            CREATOR_ID,
            () -> NOW);

    UUID actorId = UUID.fromString("10000000-0000-0000-0000-000000000000");
    Instant later = NOW.plusSeconds(60);

    task.changeStatus(TaskStatus.IN_PROGRESS, actorId, () -> later);
    List<?> events = task.drainEvents();

    assertThat(task.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
    assertThat(task.getUpdatedBy()).contains(actorId);
    assertThat(task.getUpdatedAt()).isEqualTo(later);
    assertThat(events).hasSize(1);
    assertThat(events.get(0))
        .isInstanceOfSatisfying(
            TaskStatusChangedEvent.class,
            event -> {
              assertThat(event.previousStatus()).isEqualTo(TaskStatus.TODO);
              assertThat(event.newStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
              assertThat(event.actorId()).isEqualTo(actorId);
              assertThat(event.taskId()).isEqualTo(TASK_ID);
            });
    assertThat(task.getCompletedAt()).isEmpty();
  }

  @Test
  void changeStatusRejectsInvalidTransition() {
    Task task =
        Task.create(
            TASK_ID,
            PROJECT_ID,
            "Build API",
            null,
            TaskPriority.HIGH,
            TaskStatus.TODO,
            null,
            null,
            CREATOR_ID,
            () -> NOW);

    UUID actorId = UUID.fromString("20000000-0000-0000-0000-000000000000");

    assertThatThrownBy(() -> task.changeStatus(TaskStatus.DONE, actorId, () -> NOW))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Cannot transition task");
  }

  @Test
  void changeStatusToDoneSetsCompletedAt() {
    Task task =
        Task.create(
            TASK_ID,
            PROJECT_ID,
            "Build API",
            null,
            TaskPriority.HIGH,
            TaskStatus.IN_PROGRESS,
            null,
            null,
            CREATOR_ID,
            () -> NOW);

    UUID actorId = UUID.fromString("40000000-0000-0000-0000-000000000000");
    Instant doneTime = NOW.plusSeconds(300);

    task.changeStatus(TaskStatus.DONE, actorId, () -> doneTime);

    assertThat(task.getCompletedAt()).contains(doneTime);
  }

  @Test
  void assignToPublishesEvent() {
    Task task =
        Task.create(
            TASK_ID,
            PROJECT_ID,
            "Build API",
            null,
            TaskPriority.HIGH,
            TaskStatus.TODO,
            null,
            null,
            CREATOR_ID,
            () -> NOW);

    UUID actorId = UUID.fromString("30000000-0000-0000-0000-000000000000");
    UUID assignee = UUID.fromString("f0000000-0000-0000-0000-000000000000");
    Instant later = NOW.plusSeconds(120);

    task.assignTo(assignee, actorId, () -> later);
    List<?> events = task.drainEvents();

    assertThat(task.getAssigneeId()).contains(assignee);
    assertThat(events).hasSize(1);
    assertThat(events.get(0))
        .isInstanceOfSatisfying(
            TaskAssignmentChangedEvent.class,
            event -> {
              assertThat(event.previousAssignee()).isNull();
              assertThat(event.newAssignee()).isEqualTo(assignee);
              assertThat(event.actorId()).isEqualTo(actorId);
              assertThat(event.taskId()).isEqualTo(TASK_ID);
            });
  }
}
