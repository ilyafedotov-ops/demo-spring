package com.example.taskify.domain.activity;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.util.Assert;

public record ActivityEntry(
    UUID id,
    String entityType,
    UUID entityId,
    UUID actorId,
    String action,
    Map<String, Object> payload,
    Instant occurredAt) {

  public ActivityEntry {
    Objects.requireNonNull(id, "id must not be null");
    Assert.hasText(entityType, "entityType must not be blank");
    Objects.requireNonNull(entityId, "entityId must not be null");
    Assert.hasText(action, "action must not be blank");
    Objects.requireNonNull(occurredAt, "occurredAt must not be null");
    payload = normalisePayload(payload);
  }

  private static Map<String, Object> normalisePayload(Map<String, Object> source) {
    if (source == null || source.isEmpty()) {
      return Map.of();
    }
    Map<String, Object> copy = new LinkedHashMap<>(source.size());
    source.forEach(
        (key, value) -> {
          Objects.requireNonNull(key, "payload key must not be null");
          copy.put(key, value);
        });
    return Collections.unmodifiableMap(copy);
  }
}
