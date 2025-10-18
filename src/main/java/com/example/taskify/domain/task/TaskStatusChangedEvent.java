package com.example.taskify.domain.task;

import com.example.taskify.domain.common.DomainEvent;
import java.time.Instant;
import java.util.UUID;

public record TaskStatusChangedEvent(
    UUID taskId, TaskStatus previousStatus, TaskStatus newStatus, UUID actorId, Instant occurredAt)
    implements DomainEvent {}
