package com.example.taskify.application.service;

import com.example.taskify.application.exception.ResourceNotFoundException;
import com.example.taskify.application.port.out.TagRepository;
import com.example.taskify.application.port.out.TaskEventPublisher;
import com.example.taskify.application.port.out.TaskRepository;
import com.example.taskify.application.port.out.TaskTagRepository;
import com.example.taskify.application.port.out.UserDirectory;
import com.example.taskify.domain.task.Task;
import com.example.taskify.domain.task.TaskPriority;
import com.example.taskify.domain.task.TaskStatus;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

@Service
public class TaskService {

  private final TaskRepository taskRepository;
  private final TaskEventPublisher eventPublisher;
  private final UserDirectory userDirectory;
  private final ActivityService activityService;
  private final TaskTagRepository taskTagRepository;
  private final TagRepository tagRepository;
  private final Supplier<UUID> uuidSupplier;
  private final Clock clock;

  public TaskService(
      TaskRepository taskRepository,
      TaskEventPublisher eventPublisher,
      UserDirectory userDirectory,
      TaskTagRepository taskTagRepository,
      TagRepository tagRepository,
      ActivityService activityService,
      Supplier<UUID> uuidSupplier,
      Clock clock) {
    this.taskRepository = taskRepository;
    this.eventPublisher = eventPublisher;
    this.userDirectory = userDirectory;
    this.taskTagRepository = taskTagRepository;
    this.tagRepository = tagRepository;
    this.activityService = activityService;
    this.uuidSupplier = uuidSupplier;
    this.clock = clock;
  }

  @Transactional
  public Task createTask(
      UUID projectId,
      String title,
      String description,
      TaskPriority priority,
      TaskStatus status,
      UUID assigneeId,
      LocalDate dueDate,
      UUID creatorId) {
    Assert.notNull(projectId, "projectId must not be null");
    Assert.notNull(creatorId, "creatorId must not be null");
    if (assigneeId != null && !userDirectory.existsById(assigneeId)) {
      throw new ResourceNotFoundException("Assignee does not exist");
    }
    Task task =
        Task.create(
            uuidSupplier.get(),
            projectId,
            title,
            description,
            priority == null ? TaskPriority.MEDIUM : priority,
            status,
            assigneeId,
            dueDate,
            creatorId,
            this::now);
    Task persisted = taskRepository.save(task);
    eventPublisher.publish(persisted.drainEvents());
    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("title", persisted.getTitle());
    payload.put("projectId", persisted.getProjectId());
    payload.put("status", persisted.getStatus().name());
    activityService.record(
        ActivityCodebook.ENTITY_TASK,
        persisted.getId(),
        creatorId,
        ActivityCodebook.ACTION_TASK_CREATED,
        payload);
    return persisted;
  }

  @Transactional
  public Task changeStatus(UUID taskId, TaskStatus newStatus, UUID actorId) {
    Task task =
        taskRepository
            .findById(taskId)
            .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
    TaskStatus previousStatus = task.getStatus();
    task.changeStatus(newStatus, actorId, this::now);
    Task persisted = taskRepository.save(task);
    eventPublisher.publish(persisted.drainEvents());
    if (!Objects.equals(previousStatus, persisted.getStatus())) {
      Map<String, Object> payload = new LinkedHashMap<>();
      payload.put("from", previousStatus.name());
      payload.put("to", persisted.getStatus().name());
      activityService.record(
          ActivityCodebook.ENTITY_TASK,
          persisted.getId(),
          actorId,
          ActivityCodebook.ACTION_TASK_STATUS_CHANGED,
          payload);
    }
    return persisted;
  }

  @Transactional
  public Task assignTask(UUID taskId, UUID assigneeId, UUID actorId) {
    if (assigneeId != null && !userDirectory.existsById(assigneeId)) {
      throw new ResourceNotFoundException("Assignee does not exist");
    }
    Task task =
        taskRepository
            .findById(taskId)
            .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
    UUID previousAssignee = task.getAssigneeId().orElse(null);
    task.assignTo(assigneeId, actorId, this::now);
    Task persisted = taskRepository.save(task);
    eventPublisher.publish(persisted.drainEvents());
    UUID newAssignee = persisted.getAssigneeId().orElse(null);
    if (!Objects.equals(previousAssignee, newAssignee)) {
      Map<String, Object> payload = new LinkedHashMap<>();
      payload.put("from", previousAssignee);
      payload.put("to", newAssignee);
      activityService.record(
          ActivityCodebook.ENTITY_TASK,
          persisted.getId(),
          actorId,
          ActivityCodebook.ACTION_TASK_ASSIGNED,
          payload);
    }
    return persisted;
  }

  @Transactional
  public Task updateDetails(
      UUID taskId,
      String title,
      String description,
      TaskPriority priority,
      LocalDate dueDate,
      UUID actorId) {
    Task task =
        taskRepository
            .findById(taskId)
            .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
    task.updateDetails(title, description, priority, dueDate, actorId, this::now);
    Task persisted = taskRepository.save(task);
    eventPublisher.publish(persisted.drainEvents());
    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("title", persisted.getTitle());
    payload.put("priority", persisted.getPriority().name());
    payload.put("dueDate", persisted.getDueDate().map(LocalDate::toString).orElse(null));
    activityService.record(
        ActivityCodebook.ENTITY_TASK,
        persisted.getId(),
        actorId,
        ActivityCodebook.ACTION_TASK_UPDATED,
        payload);
    return persisted;
  }

  private Instant now() {
    return clock.instant();
  }

  @Transactional
  public void attachTag(UUID taskId, UUID tagId, UUID actorId) {
    Assert.notNull(taskId, "taskId must not be null");
    Assert.notNull(tagId, "tagId must not be null");
    Assert.notNull(actorId, "actorId must not be null");
    Task task =
        taskRepository
            .findById(taskId)
            .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
    var tag =
        tagRepository
            .findById(tagId)
            .orElseThrow(() -> new ResourceNotFoundException("Tag not found"));
    if (taskTagRepository.isTagAttached(task.getId(), tag.getId())) {
      return;
    }
    taskTagRepository.attach(task.getId(), tag.getId());
    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("tagId", tag.getId());
    payload.put("tagName", tag.getName());
    payload.put("tagColor", tag.getColor());
    activityService.record(
        ActivityCodebook.ENTITY_TASK,
        task.getId(),
        actorId,
        ActivityCodebook.ACTION_TASK_TAGGED,
        payload);
  }

  @Transactional
  public void detachTag(UUID taskId, UUID tagId, UUID actorId) {
    Assert.notNull(taskId, "taskId must not be null");
    Assert.notNull(tagId, "tagId must not be null");
    Assert.notNull(actorId, "actorId must not be null");
    Task task =
        taskRepository
            .findById(taskId)
            .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
    var tag =
        tagRepository
            .findById(tagId)
            .orElseThrow(() -> new ResourceNotFoundException("Tag not found"));
    boolean removed = taskTagRepository.detach(task.getId(), tag.getId());
    if (!removed) {
      return;
    }
    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("tagId", tag.getId());
    payload.put("tagName", tag.getName());
    payload.put("tagColor", tag.getColor());
    activityService.record(
        ActivityCodebook.ENTITY_TASK,
        task.getId(),
        actorId,
        ActivityCodebook.ACTION_TASK_UNTAGGED,
        payload);
  }

  @Transactional(readOnly = true)
  public Task getTask(UUID taskId) {
    return taskRepository
        .findById(taskId)
        .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
  }

  @Transactional(readOnly = true)
  public List<Task> listTasks(UUID projectId) {
    Assert.notNull(projectId, "projectId must not be null");
    return taskRepository.findByProjectId(projectId);
  }
}
