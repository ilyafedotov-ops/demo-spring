package com.example.taskify.infrastructure.events;

import com.example.taskify.application.port.out.ProjectEventPublisher;
import com.example.taskify.application.port.out.TaskEventPublisher;
import com.example.taskify.domain.common.DomainEvent;
import java.util.Collection;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class SpringDomainEventPublisher implements TaskEventPublisher, ProjectEventPublisher {

  private final ApplicationEventPublisher eventPublisher;

  public SpringDomainEventPublisher(ApplicationEventPublisher eventPublisher) {
    this.eventPublisher = eventPublisher;
  }

  @Override
  public void publish(Collection<DomainEvent> events) {
    if (events == null || events.isEmpty()) {
      return;
    }
    events.forEach(eventPublisher::publishEvent);
  }
}
