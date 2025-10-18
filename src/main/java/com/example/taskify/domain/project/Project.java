package com.example.taskify.domain.project;

import com.example.taskify.domain.common.AggregateRoot;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import org.springframework.util.Assert;

public class Project extends AggregateRoot {
  private final UUID id;
  private String name;
  private String description;
  private UUID ownerId;
  private ProjectStatus status;
  private LocalDate startDate;
  private LocalDate endDate;
  private final UUID createdBy;
  private final Instant createdAt;
  private UUID updatedBy;
  private Instant updatedAt;

  private Project(
      UUID id,
      String name,
      String description,
      UUID ownerId,
      ProjectStatus status,
      LocalDate startDate,
      LocalDate endDate,
      UUID createdBy,
      Instant createdAt,
      UUID updatedBy,
      Instant updatedAt) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.ownerId = ownerId;
    this.status = status;
    this.startDate = startDate;
    this.endDate = endDate;
    this.createdBy = createdBy;
    this.createdAt = createdAt;
    this.updatedBy = updatedBy;
    this.updatedAt = updatedAt;
  }

  public static Project create(
      UUID id,
      String name,
      String description,
      UUID ownerId,
      ProjectStatus initialStatus,
      LocalDate startDate,
      LocalDate endDate,
      UUID creatorId,
      Supplier<Instant> timestampSupplier) {
    Objects.requireNonNull(id, "id must not be null");
    Assert.hasText(name, "name must not be blank");
    Objects.requireNonNull(ownerId, "ownerId must not be null");
    Objects.requireNonNull(creatorId, "creatorId must not be null");
    if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
      throw new IllegalArgumentException("endDate must be after startDate");
    }
    Instant now = Objects.requireNonNull(timestampSupplier.get(), "timestamp must not be null");
    Project project =
        new Project(
            id,
            name.strip(),
            description,
            ownerId,
            initialStatus == null ? ProjectStatus.PLANNED : initialStatus,
            startDate,
            endDate,
            creatorId,
            now,
            creatorId,
            now);
    return project;
  }

  public static Project rehydrate(
      UUID id,
      String name,
      String description,
      UUID ownerId,
      ProjectStatus status,
      LocalDate startDate,
      LocalDate endDate,
      UUID createdBy,
      Instant createdAt,
      UUID updatedBy,
      Instant updatedAt) {
    Objects.requireNonNull(id, "id must not be null");
    Objects.requireNonNull(name, "name must not be null");
    Objects.requireNonNull(ownerId, "ownerId must not be null");
    Objects.requireNonNull(status, "status must not be null");
    Objects.requireNonNull(createdBy, "createdBy must not be null");
    Objects.requireNonNull(createdAt, "createdAt must not be null");
    Objects.requireNonNull(updatedAt, "updatedAt must not be null");
    return new Project(
        id,
        name,
        description,
        ownerId,
        status,
        startDate,
        endDate,
        createdBy,
        createdAt,
        updatedBy,
        updatedAt);
  }

  public UUID getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public UUID getOwnerId() {
    return ownerId;
  }

  public ProjectStatus getStatus() {
    return status;
  }

  public Optional<LocalDate> getStartDate() {
    return Optional.ofNullable(startDate);
  }

  public Optional<LocalDate> getEndDate() {
    return Optional.ofNullable(endDate);
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

  public void rename(String newName, String newDescription, UUID actorId, Supplier<Instant> now) {
    Assert.hasText(newName, "newName must not be blank");
    Objects.requireNonNull(actorId, "actorId must not be null");
    this.name = newName.strip();
    this.description = newDescription;
    updateAudit(actorId, now);
  }

  public void changeOwner(UUID newOwnerId, UUID actorId, Supplier<Instant> now) {
    Objects.requireNonNull(newOwnerId, "newOwnerId must not be null");
    Objects.requireNonNull(actorId, "actorId must not be null");
    if (!Objects.equals(this.ownerId, newOwnerId)) {
      this.ownerId = newOwnerId;
      updateAudit(actorId, now);
    }
  }

  public void schedule(
      LocalDate newStartDate, LocalDate newEndDate, UUID actorId, Supplier<Instant> now) {
    if (newStartDate != null && newEndDate != null && newEndDate.isBefore(newStartDate)) {
      throw new IllegalArgumentException("endDate must be after startDate");
    }
    this.startDate = newStartDate;
    this.endDate = newEndDate;
    updateAudit(actorId, now);
  }

  public void changeStatus(ProjectStatus newStatus, UUID actorId, Supplier<Instant> now) {
    Objects.requireNonNull(newStatus, "newStatus must not be null");
    Objects.requireNonNull(actorId, "actorId must not be null");
    if (newStatus == status) {
      return;
    }
    if (!status.canTransitionTo(newStatus)) {
      throw new IllegalStateException(
          "Cannot transition project %s from %s to %s"
              .formatted(id, status.name(), newStatus.name()));
    }
    ProjectStatus previous = this.status;
    this.status = newStatus;
    Instant timestamp = updateAudit(actorId, now);
    registerEvent(new ProjectStatusChangedEvent(id, previous, newStatus, actorId, timestamp));
  }

  private Instant updateAudit(UUID actorId, Supplier<Instant> nowSupplier) {
    Instant now = Objects.requireNonNull(nowSupplier.get(), "timestamp must not be null");
    this.updatedBy = actorId;
    this.updatedAt = now;
    return now;
  }
}
