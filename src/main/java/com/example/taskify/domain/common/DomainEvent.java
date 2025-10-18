package com.example.taskify.domain.common;

import java.time.Instant;

public interface DomainEvent {
  Instant occurredAt();
}
