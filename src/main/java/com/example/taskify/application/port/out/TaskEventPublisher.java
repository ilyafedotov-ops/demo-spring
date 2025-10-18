package com.example.taskify.application.port.out;

import com.example.taskify.domain.common.DomainEvent;
import java.util.Collection;

public interface TaskEventPublisher {
  void publish(Collection<DomainEvent> events);
}
