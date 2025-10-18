package com.example.taskify.domain.task;

import com.example.taskify.domain.common.DomainEvent;
import java.time.Instant;
import java.util.UUID;

public record TaskAssignmentChangedEvent(
    UUID taskId, UUID previousAssignee, UUID newAssignee, UUID actorId, Instant occurredAt)
    implements DomainEvent {}
