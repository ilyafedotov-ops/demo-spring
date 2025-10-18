package com.example.taskify.domain.project;

import com.example.taskify.domain.common.DomainEvent;
import java.time.Instant;
import java.util.UUID;

public record ProjectStatusChangedEvent(
    UUID projectId,
    ProjectStatus previousStatus,
    ProjectStatus newStatus,
    UUID actorId,
    Instant occurredAt)
    implements DomainEvent {}
