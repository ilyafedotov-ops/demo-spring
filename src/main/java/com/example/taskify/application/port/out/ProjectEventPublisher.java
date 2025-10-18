package com.example.taskify.application.port.out;

import com.example.taskify.domain.common.DomainEvent;
import java.util.Collection;

public interface ProjectEventPublisher {
  void publish(Collection<DomainEvent> events);
}
