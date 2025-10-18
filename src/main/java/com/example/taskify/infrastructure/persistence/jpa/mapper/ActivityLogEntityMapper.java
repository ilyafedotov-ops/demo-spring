package com.example.taskify.infrastructure.persistence.jpa.mapper;

import com.example.taskify.domain.activity.ActivityEntry;
import com.example.taskify.infrastructure.persistence.jpa.entity.ActivityLogJpaEntity;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class ActivityLogEntityMapper {

  public ActivityLogJpaEntity toEntity(ActivityEntry entry) {
    ActivityLogJpaEntity entity = new ActivityLogJpaEntity();
    entity.setId(entry.id());
    entity.setEntityType(entry.entityType());
    entity.setEntityId(entry.entityId());
    entity.setActorId(entry.actorId());
    entity.setAction(entry.action());
    entity.setOccurredAt(entry.occurredAt());
    entity.setPayload(entry.payload());
    return entity;
  }

  public ActivityEntry toDomain(ActivityLogJpaEntity entity) {
    return new ActivityEntry(
        entity.getId(),
        entity.getEntityType(),
        entity.getEntityId(),
        entity.getActorId(),
        entity.getAction(),
        entity.getPayload() == null ? Map.of() : entity.getPayload(),
        entity.getOccurredAt());
  }
}
