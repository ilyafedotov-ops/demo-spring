package com.example.taskify.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.taskify.application.port.out.TagRepository;
import com.example.taskify.application.port.out.TaskEventPublisher;
import com.example.taskify.application.port.out.TaskRepository;
import com.example.taskify.application.port.out.TaskTagRepository;
import com.example.taskify.application.port.out.UserDirectory;
import com.example.taskify.domain.common.DomainEvent;
import com.example.taskify.domain.tag.Tag;
import com.example.taskify.domain.task.Task;
import com.example.taskify.domain.task.TaskPriority;
import com.example.taskify.domain.task.TaskStatus;
import com.example.taskify.domain.task.TaskStatusChangedEvent;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

  private static final Instant NOW = Instant.parse("2025-01-01T00:00:00Z");
  private static final UUID TASK_ID = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
  private static final UUID PROJECT_ID = UUID.fromString("11111111-2222-3333-4444-555555555555");
  private static final UUID CREATOR_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final UUID ACTOR_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

  @Mock private TaskRepository taskRepository;
  @Mock private TaskEventPublisher eventPublisher;
  @Mock private UserDirectory userDirectory;
  @Mock private TaskTagRepository taskTagRepository;
  @Mock private TagRepository tagRepository;
  @Mock private ActivityService activityService;

  private TaskService taskService;
  private Supplier<UUID> uuidSupplier;

  @BeforeEach
  void setUp() {
    uuidSupplier = () -> TASK_ID;
    taskService =
        new TaskService(
            taskRepository,
            eventPublisher,
            userDirectory,
            taskTagRepository,
            tagRepository,
            activityService,
            uuidSupplier,
            Clock.fixed(NOW, ZoneOffset.UTC));
  }

  @Test
  void createTaskPersistsAndPublishes() {
    when(userDirectory.existsById(any())).thenReturn(true);
    when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(activityService.record(any(), any(), any(), any(), any())).thenAnswer(invocation -> null);

    Task created =
        taskService.createTask(
            PROJECT_ID,
            "New task",
            "Description",
            TaskPriority.HIGH,
            TaskStatus.TODO,
            ACTOR_ID,
            LocalDate.of(2025, 2, 1),
            CREATOR_ID);

    assertThat(created.getId()).isEqualTo(TASK_ID);
    assertThat(created.getPriority()).isEqualTo(TaskPriority.HIGH);
    verify(taskRepository).save(any(Task.class));
    verify(userDirectory).existsById(ACTOR_ID);
    ArgumentCaptor<Collection<DomainEvent>> eventsCaptor =
        ArgumentCaptor.forClass(Collection.class);
    verify(eventPublisher).publish(eventsCaptor.capture());
    assertThat(eventsCaptor.getValue()).isEmpty();
    ArgumentCaptor<Map<String, Object>> payloadCaptor = ArgumentCaptor.forClass(Map.class);
    verify(activityService)
        .record(
            eq(ActivityCodebook.ENTITY_TASK),
            eq(TASK_ID),
            eq(CREATOR_ID),
            eq(ActivityCodebook.ACTION_TASK_CREATED),
            payloadCaptor.capture());
    assertThat(payloadCaptor.getValue())
        .containsEntry("title", "New task")
        .containsEntry("projectId", PROJECT_ID)
        .containsEntry("status", TaskStatus.TODO.name());
  }

  @Test
  void changeStatusEmitsDomainEvent() {
    Task task =
        Task.create(
            TASK_ID,
            PROJECT_ID,
            "Task",
            null,
            TaskPriority.MEDIUM,
            TaskStatus.TODO,
            null,
            null,
            CREATOR_ID,
            () -> NOW);
    when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));
    when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(activityService.record(any(), any(), any(), any(), any())).thenAnswer(invocation -> null);

    taskService.changeStatus(TASK_ID, TaskStatus.IN_PROGRESS, ACTOR_ID);

    ArgumentCaptor<Collection<DomainEvent>> captor = ArgumentCaptor.forClass(Collection.class);
    verify(eventPublisher).publish(captor.capture());
    assertThat(captor.getValue())
        .singleElement()
        .isInstanceOfSatisfying(
            TaskStatusChangedEvent.class,
            event -> {
              assertThat(event.previousStatus()).isEqualTo(TaskStatus.TODO);
              assertThat(event.newStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
            });
    ArgumentCaptor<Map<String, Object>> payloadCaptor = ArgumentCaptor.forClass(Map.class);
    verify(activityService)
        .record(
            eq(ActivityCodebook.ENTITY_TASK),
            eq(TASK_ID),
            eq(ACTOR_ID),
            eq(ActivityCodebook.ACTION_TASK_STATUS_CHANGED),
            payloadCaptor.capture());
    assertThat(payloadCaptor.getValue())
        .containsEntry("from", TaskStatus.TODO.name())
        .containsEntry("to", TaskStatus.IN_PROGRESS.name());
  }

  @Test
  void assignTaskValidatesUser() {
    Task task =
        Task.create(
            TASK_ID,
            PROJECT_ID,
            "Task",
            null,
            TaskPriority.MEDIUM,
            TaskStatus.TODO,
            null,
            null,
            CREATOR_ID,
            () -> NOW);
    UUID assigneeId = UUID.fromString("00000000-0000-0000-0000-000000000003");
    when(userDirectory.existsById(assigneeId)).thenReturn(true);
    when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));
    when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(activityService.record(any(), any(), any(), any(), any())).thenAnswer(invocation -> null);

    taskService.assignTask(TASK_ID, assigneeId, ACTOR_ID);

    verify(taskRepository).save(any(Task.class));
    verify(userDirectory).existsById(assigneeId);
    ArgumentCaptor<Map<String, Object>> payloadCaptor = ArgumentCaptor.forClass(Map.class);
    verify(activityService)
        .record(
            eq(ActivityCodebook.ENTITY_TASK),
            eq(TASK_ID),
            eq(ACTOR_ID),
            eq(ActivityCodebook.ACTION_TASK_ASSIGNED),
            payloadCaptor.capture());
    Map<String, Object> payload = payloadCaptor.getValue();
    assertThat(payload).containsKey("from");
    assertThat(payload.get("from")).isNull();
    assertThat(payload).containsEntry("to", assigneeId);
  }

  @Test
  void attachTagRecordsActivityWhenSuccessful() {
    UUID tagId = UUID.fromString("00000000-0000-0000-0000-000000000010");
    UUID actorId = UUID.fromString("00000000-0000-0000-0000-000000000020");
    Task task =
        Task.create(
            TASK_ID,
            PROJECT_ID,
            "Task",
            null,
            TaskPriority.MEDIUM,
            TaskStatus.TODO,
            null,
            null,
            CREATOR_ID,
            () -> NOW);
    Tag tag = Tag.create(tagId, "Backend", "#FFFFFF", NOW);
    when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));
    when(tagRepository.findById(tagId)).thenReturn(Optional.of(tag));
    when(taskTagRepository.isTagAttached(TASK_ID, tagId)).thenReturn(false);
    when(activityService.record(any(), any(), any(), any(), any())).thenAnswer(invocation -> null);

    taskService.attachTag(TASK_ID, tagId, actorId);

    verify(taskTagRepository).attach(TASK_ID, tagId);
    ArgumentCaptor<Map<String, Object>> payloadCaptor = ArgumentCaptor.forClass(Map.class);
    verify(activityService)
        .record(
            eq(ActivityCodebook.ENTITY_TASK),
            eq(TASK_ID),
            eq(actorId),
            eq(ActivityCodebook.ACTION_TASK_TAGGED),
            payloadCaptor.capture());
    Map<String, Object> payload = payloadCaptor.getValue();
    assertThat(payload).containsEntry("tagId", tagId);
    assertThat(payload).containsEntry("tagName", "Backend");
    assertThat(payload).containsEntry("tagColor", "#FFFFFF");
  }

  @Test
  void attachTagSkipsWhenAlreadyAttached() {
    UUID tagId = UUID.fromString("00000000-0000-0000-0000-000000000011");
    Task task =
        Task.create(
            TASK_ID,
            PROJECT_ID,
            "Task",
            null,
            TaskPriority.MEDIUM,
            TaskStatus.TODO,
            null,
            null,
            CREATOR_ID,
            () -> NOW);
    Tag tag = Tag.create(tagId, "Backend", "#FFFFFF", NOW);
    when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));
    when(tagRepository.findById(tagId)).thenReturn(Optional.of(tag));
    when(taskTagRepository.isTagAttached(TASK_ID, tagId)).thenReturn(true);

    taskService.attachTag(TASK_ID, tagId, ACTOR_ID);

    verify(taskTagRepository, never()).attach(any(), any());
    verify(activityService, never())
        .record(any(), any(), any(), eq(ActivityCodebook.ACTION_TASK_TAGGED), any());
  }

  @Test
  void detachTagRecordsActivityWhenAssociationRemoved() {
    UUID tagId = UUID.fromString("00000000-0000-0000-0000-000000000012");
    Tag tag = Tag.create(tagId, "Backend", "#FFFFFF", NOW);
    Task task =
        Task.create(
            TASK_ID,
            PROJECT_ID,
            "Task",
            null,
            TaskPriority.MEDIUM,
            TaskStatus.TODO,
            null,
            null,
            CREATOR_ID,
            () -> NOW);
    when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));
    when(tagRepository.findById(tagId)).thenReturn(Optional.of(tag));
    when(taskTagRepository.detach(TASK_ID, tagId)).thenReturn(true);
    when(activityService.record(any(), any(), any(), any(), any())).thenAnswer(invocation -> null);

    taskService.detachTag(TASK_ID, tagId, ACTOR_ID);

    verify(taskTagRepository).detach(TASK_ID, tagId);
    ArgumentCaptor<Map<String, Object>> payloadCaptor = ArgumentCaptor.forClass(Map.class);
    verify(activityService)
        .record(
            eq(ActivityCodebook.ENTITY_TASK),
            eq(TASK_ID),
            eq(ACTOR_ID),
            eq(ActivityCodebook.ACTION_TASK_UNTAGGED),
            payloadCaptor.capture());
    Map<String, Object> payload = payloadCaptor.getValue();
    assertThat(payload).containsEntry("tagId", tagId);
    assertThat(payload).containsEntry("tagName", "Backend");
    assertThat(payload).containsEntry("tagColor", "#FFFFFF");
  }

  @Test
  void detachTagSkipsWhenNoAssociationRemoved() {
    UUID tagId = UUID.fromString("00000000-0000-0000-0000-000000000013");
    Tag tag = Tag.create(tagId, "Backend", "#FFFFFF", NOW);
    Task task =
        Task.create(
            TASK_ID,
            PROJECT_ID,
            "Task",
            null,
            TaskPriority.MEDIUM,
            TaskStatus.TODO,
            null,
            null,
            CREATOR_ID,
            () -> NOW);
    when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));
    when(tagRepository.findById(tagId)).thenReturn(Optional.of(tag));
    when(taskTagRepository.detach(TASK_ID, tagId)).thenReturn(false);

    taskService.detachTag(TASK_ID, tagId, ACTOR_ID);

    verify(activityService, never())
        .record(any(), any(), any(), eq(ActivityCodebook.ACTION_TASK_UNTAGGED), any());
  }

  @Test
  void attachTagRejectsUnknownTask() {
    UUID tagId = UUID.randomUUID();
    when(taskRepository.findById(TASK_ID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> taskService.attachTag(TASK_ID, tagId, ACTOR_ID))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Task not found");
  }

  @Test
  void attachTagRejectsUnknownTag() {
    UUID tagId = UUID.randomUUID();
    Task task =
        Task.create(
            TASK_ID,
            PROJECT_ID,
            "Task",
            null,
            TaskPriority.MEDIUM,
            TaskStatus.TODO,
            null,
            null,
            CREATOR_ID,
            () -> NOW);
    when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));
    when(tagRepository.findById(tagId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> taskService.attachTag(TASK_ID, tagId, ACTOR_ID))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Tag not found");
  }

  @Test
  void detachTagRejectsUnknownTask() {
    UUID tagId = UUID.randomUUID();
    when(taskRepository.findById(TASK_ID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> taskService.detachTag(TASK_ID, tagId, ACTOR_ID))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Task not found");
  }

  @Test
  void detachTagRejectsUnknownTag() {
    UUID tagId = UUID.randomUUID();
    Task task =
        Task.create(
            TASK_ID,
            PROJECT_ID,
            "Task",
            null,
            TaskPriority.MEDIUM,
            TaskStatus.TODO,
            null,
            null,
            CREATOR_ID,
            () -> NOW);
    when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));
    when(tagRepository.findById(tagId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> taskService.detachTag(TASK_ID, tagId, ACTOR_ID))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Tag not found");
  }

  @Test
  void assignTaskRejectsUnknownUser() {
    when(userDirectory.existsById(ACTOR_ID)).thenReturn(false);

    assertThatThrownBy(() -> taskService.assignTask(TASK_ID, ACTOR_ID, CREATOR_ID))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Assignee does not exist");

    verify(taskRepository, never()).findById(any());
    verify(eventPublisher, never()).publish(any());
  }

  @Test
  void assignTaskUnassignsPreservingPayloadShape() {
    UUID previousAssignee = UUID.fromString("00000000-0000-0000-0000-000000000004");
    Task task =
        Task.create(
            TASK_ID,
            PROJECT_ID,
            "Task",
            null,
            TaskPriority.MEDIUM,
            TaskStatus.TODO,
            previousAssignee,
            null,
            CREATOR_ID,
            () -> NOW);
    when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));
    when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(activityService.record(any(), any(), any(), any(), any())).thenAnswer(invocation -> null);

    taskService.assignTask(TASK_ID, null, ACTOR_ID);

    ArgumentCaptor<Map<String, Object>> payloadCaptor = ArgumentCaptor.forClass(Map.class);
    verify(activityService)
        .record(
            eq(ActivityCodebook.ENTITY_TASK),
            eq(TASK_ID),
            eq(ACTOR_ID),
            eq(ActivityCodebook.ACTION_TASK_ASSIGNED),
            payloadCaptor.capture());
    Map<String, Object> payload = payloadCaptor.getValue();
    assertThat(payload).containsEntry("from", previousAssignee);
    assertThat(payload).containsKey("to");
    assertThat(payload.get("to")).isNull();
  }

  @Test
  void changeStatusNoopSkipsActivity() {
    Task task =
        Task.create(
            TASK_ID,
            PROJECT_ID,
            "Task",
            null,
            TaskPriority.MEDIUM,
            TaskStatus.TODO,
            null,
            null,
            CREATOR_ID,
            () -> NOW);
    when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));
    when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

    taskService.changeStatus(TASK_ID, TaskStatus.TODO, ACTOR_ID);

    verify(activityService, never())
        .record(any(), any(), any(), eq(ActivityCodebook.ACTION_TASK_STATUS_CHANGED), any());
  }

  @Test
  void assignTaskNoopSkipsActivity() {
    UUID assigneeId = UUID.fromString("00000000-0000-0000-0000-000000000005");
    Task task =
        Task.create(
            TASK_ID,
            PROJECT_ID,
            "Task",
            null,
            TaskPriority.MEDIUM,
            TaskStatus.TODO,
            assigneeId,
            null,
            CREATOR_ID,
            () -> NOW);
    when(userDirectory.existsById(assigneeId)).thenReturn(true);
    when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));
    when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

    taskService.assignTask(TASK_ID, assigneeId, ACTOR_ID);

    verify(activityService, never())
        .record(
            eq(ActivityCodebook.ENTITY_TASK),
            eq(TASK_ID),
            any(),
            eq(ActivityCodebook.ACTION_TASK_ASSIGNED),
            any());
  }

  @Test
  void updateDetailsRecordsPayloadWithNullDueDate() {
    Task task =
        Task.create(
            TASK_ID,
            PROJECT_ID,
            "Task",
            null,
            TaskPriority.MEDIUM,
            TaskStatus.TODO,
            null,
            LocalDate.of(2025, 2, 1),
            CREATOR_ID,
            () -> NOW);
    when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));
    when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(activityService.record(any(), any(), any(), any(), any())).thenAnswer(invocation -> null);

    taskService.updateDetails(
        TASK_ID, " Revised title ", "Updated description", TaskPriority.LOW, null, ACTOR_ID);

    ArgumentCaptor<Map<String, Object>> payloadCaptor = ArgumentCaptor.forClass(Map.class);
    verify(activityService)
        .record(
            eq(ActivityCodebook.ENTITY_TASK),
            eq(TASK_ID),
            eq(ACTOR_ID),
            eq(ActivityCodebook.ACTION_TASK_UPDATED),
            payloadCaptor.capture());
    Map<String, Object> payload = payloadCaptor.getValue();
    assertThat(payload).containsEntry("title", "Revised title");
    assertThat(payload).containsEntry("priority", TaskPriority.LOW.name());
    assertThat(payload).containsKey("dueDate");
    assertThat(payload.get("dueDate")).isNull();
  }

  @Test
  void getTaskReturnsExistingTask() {
    Task task =
        Task.create(
            TASK_ID,
            PROJECT_ID,
            "Task",
            null,
            TaskPriority.MEDIUM,
            TaskStatus.TODO,
            null,
            null,
            CREATOR_ID,
            () -> NOW);
    when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));

    Task result = taskService.getTask(TASK_ID);

    assertThat(result).isSameAs(task);
  }

  @Test
  void getTaskThrowsWhenMissing() {
    when(taskRepository.findById(TASK_ID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> taskService.getTask(TASK_ID))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Task not found");
  }

  @Test
  void listTasksDelegatesToRepository() {
    Task task =
        Task.create(
            TASK_ID,
            PROJECT_ID,
            "Task",
            null,
            TaskPriority.MEDIUM,
            TaskStatus.TODO,
            null,
            null,
            CREATOR_ID,
            () -> NOW);
    when(taskRepository.findByProjectId(PROJECT_ID)).thenReturn(List.of(task));

    List<Task> results = taskService.listTasks(PROJECT_ID);

    assertThat(results).containsExactly(task);
  }
}
