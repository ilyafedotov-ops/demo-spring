package com.example.taskify.web;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record ActivityResponse(
    UUID id,
    String entityType,
    UUID entityId,
    UUID actorId,
    String action,
    Map<String, Object> payload,
    Instant occurredAt) {}
