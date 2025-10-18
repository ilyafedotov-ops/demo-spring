package com.example.taskify.infrastructure.persistence.jpa.adapter;

import com.example.taskify.application.port.out.ActivityLogRepository;
import com.example.taskify.domain.activity.ActivityEntry;
import com.example.taskify.infrastructure.persistence.jpa.mapper.ActivityLogEntityMapper;
import com.example.taskify.infrastructure.persistence.jpa.repository.ActivityLogJpaRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
public class ActivityLogRepositoryAdapter implements ActivityLogRepository {

  private final ActivityLogJpaRepository jpaRepository;
  private final ActivityLogEntityMapper mapper;

  public ActivityLogRepositoryAdapter(
      ActivityLogJpaRepository jpaRepository, ActivityLogEntityMapper mapper) {
    this.jpaRepository = jpaRepository;
    this.mapper = mapper;
  }

  @Override
  public ActivityEntry save(ActivityEntry entry) {
    var entity = mapper.toEntity(entry);
    jpaRepository.save(entity);
    return entry;
  }

  @Override
  public List<ActivityEntry> findByEntity(UUID entityId, String entityType) {
    return jpaRepository
        .findByEntityTypeAndEntityIdOrderByOccurredAtAsc(entityType, entityId)
        .stream()
        .map(mapper::toDomain)
        .toList();
  }

  @Override
  public List<ActivityEntry> findRecentByEntityType(String entityType, int limit) {
    return jpaRepository
        .findByEntityTypeOrderByOccurredAtDesc(entityType, PageRequest.of(0, limit))
        .stream()
        .map(mapper::toDomain)
        .toList();
  }
}
