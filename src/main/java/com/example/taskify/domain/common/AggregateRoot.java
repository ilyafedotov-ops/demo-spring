package com.example.taskify.domain.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AggregateRoot {
  private final List<DomainEvent> domainEvents = new ArrayList<>();

  protected void registerEvent(DomainEvent event) {
    domainEvents.add(event);
  }

  public List<DomainEvent> drainEvents() {
    if (domainEvents.isEmpty()) {
      return Collections.emptyList();
    }
    List<DomainEvent> snapshot = List.copyOf(domainEvents);
    domainEvents.clear();
    return snapshot;
  }
}
