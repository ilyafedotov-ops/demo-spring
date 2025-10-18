package com.example.taskify.domain.task;

import com.example.taskify.domain.common.AggregateRoot;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import org.springframework.util.Assert;

public class Task extends AggregateRoot {
  private final UUID id;
  private final UUID projectId;
  private String title;
  private String description;
  private TaskPriority priority;
  private TaskStatus status;
  private UUID assigneeId;
  private LocalDate dueDate;
  private Instant completedAt;
  private final UUID createdBy;
  private final Instant createdAt;
  private UUID updatedBy;
  private Instant updatedAt;

  private Task(
      UUID id,
      UUID projectId,
      String title,
      String description,
      TaskPriority priority,
      TaskStatus status,
      UUID assigneeId,
      LocalDate dueDate,
      Instant completedAt,
      UUID createdBy,
      Instant createdAt,
      UUID updatedBy,
      Instant updatedAt) {
    this.id = id;
    this.projectId = projectId;
    this.title = title;
    this.description = description;
    this.priority = priority;
    this.status = status;
    this.assigneeId = assigneeId;
    this.dueDate = dueDate;
    this.completedAt = completedAt;
    this.createdBy = createdBy;
    this.createdAt = createdAt;
    this.updatedBy = updatedBy;
    this.updatedAt = updatedAt;
  }

  public static Task create(
      UUID id,
      UUID projectId,
      String title,
      String description,
      TaskPriority priority,
      TaskStatus status,
      UUID assigneeId,
      LocalDate dueDate,
      UUID creatorId,
      Supplier<Instant> timestampSupplier) {
    Objects.requireNonNull(id, "id must not be null");
    Objects.requireNonNull(projectId, "projectId must not be null");
    Assert.hasText(title, "title must not be blank");
    Objects.requireNonNull(priority, "priority must not be null");
    Objects.requireNonNull(creatorId, "creatorId must not be null");
    Instant now = Objects.requireNonNull(timestampSupplier.get(), "timestamp must not be null");
    TaskStatus initialStatus = status == null ? TaskStatus.TODO : status;
    Instant completedAt = initialStatus == TaskStatus.DONE ? now : null;
    return new Task(
        id,
        projectId,
        title.strip(),
        description,
        priority,
        initialStatus,
        assigneeId,
        dueDate,
        completedAt,
        creatorId,
        now,
        creatorId,
        now);
  }

  public static Task rehydrate(
      UUID id,
      UUID projectId,
      String title,
      String description,
      TaskPriority priority,
      TaskStatus status,
      UUID assigneeId,
      LocalDate dueDate,
      Instant completedAt,
      UUID createdBy,
      Instant createdAt,
      UUID updatedBy,
      Instant updatedAt) {
    Objects.requireNonNull(id, "id must not be null");
    Objects.requireNonNull(projectId, "projectId must not be null");
    Objects.requireNonNull(priority, "priority must not be null");
    Objects.requireNonNull(status, "status must not be null");
    Objects.requireNonNull(createdBy, "createdBy must not be null");
    Objects.requireNonNull(createdAt, "createdAt must not be null");
    Objects.requireNonNull(updatedAt, "updatedAt must not be null");
    return new Task(
        id,
        projectId,
        title,
        description,
        priority,
        status,
        assigneeId,
        dueDate,
        completedAt,
        createdBy,
        createdAt,
        updatedBy,
        updatedAt);
  }

  public UUID getId() {
    return id;
  }

  public UUID getProjectId() {
    return projectId;
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  public TaskPriority getPriority() {
    return priority;
  }

  public TaskStatus getStatus() {
    return status;
  }

  public Optional<UUID> getAssigneeId() {
    return Optional.ofNullable(assigneeId);
  }

  public Optional<LocalDate> getDueDate() {
    return Optional.ofNullable(dueDate);
  }

  public Optional<Instant> getCompletedAt() {
    return Optional.ofNullable(completedAt);
  }

  public UUID getCreatedBy() {
    return createdBy;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Optional<UUID> getUpdatedBy() {
    return Optional.ofNullable(updatedBy);
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public void changeStatus(
      TaskStatus newStatus, UUID actorId, Supplier<Instant> timestampSupplier) {
    Objects.requireNonNull(newStatus, "newStatus must not be null");
    Objects.requireNonNull(actorId, "actorId must not be null");
    if (newStatus == status) {
      return;
    }
    if (!status.canTransitionTo(newStatus)) {
      throw new IllegalStateException(
          "Cannot transition task %s from %s to %s".formatted(id, status.name(), newStatus.name()));
    }
    TaskStatus previousStatus = this.status;
    this.status = newStatus;
    Instant now = Objects.requireNonNull(timestampSupplier.get(), "timestamp must not be null");
    this.updatedBy = actorId;
    this.updatedAt = now;
    this.completedAt = newStatus == TaskStatus.DONE ? now : null;
    registerEvent(new TaskStatusChangedEvent(id, previousStatus, newStatus, actorId, now));
  }

  public void assignTo(UUID newAssignee, UUID actorId, Supplier<Instant> timestampSupplier) {
    Objects.requireNonNull(actorId, "actorId must not be null");
    if (Objects.equals(this.assigneeId, newAssignee)) {
      return;
    }
    UUID previousAssignee = this.assigneeId;
    this.assigneeId = newAssignee;
    Instant now = Objects.requireNonNull(timestampSupplier.get(), "timestamp must not be null");
    this.updatedBy = actorId;
    this.updatedAt = now;
    registerEvent(new TaskAssignmentChangedEvent(id, previousAssignee, newAssignee, actorId, now));
  }

  public void updateDetails(
      String newTitle,
      String newDescription,
      TaskPriority newPriority,
      LocalDate newDueDate,
      UUID actorId,
      Supplier<Instant> timestampSupplier) {
    Objects.requireNonNull(actorId, "actorId must not be null");
    Assert.hasText(newTitle, "newTitle must not be blank");
    Objects.requireNonNull(newPriority, "newPriority must not be null");
    this.title = newTitle.strip();
    this.description = newDescription;
    this.priority = newPriority;
    this.dueDate = newDueDate;
    Instant now = Objects.requireNonNull(timestampSupplier.get(), "timestamp must not be null");
    this.updatedBy = actorId;
    this.updatedAt = now;
  }
}
