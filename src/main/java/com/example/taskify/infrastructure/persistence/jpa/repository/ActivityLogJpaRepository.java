package com.example.taskify.infrastructure.persistence.jpa.repository;

import com.example.taskify.infrastructure.persistence.jpa.entity.ActivityLogJpaEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityLogJpaRepository extends JpaRepository<ActivityLogJpaEntity, UUID> {
  List<ActivityLogJpaEntity> findByEntityTypeAndEntityIdOrderByOccurredAtAsc(
      String entityType, UUID entityId);

  List<ActivityLogJpaEntity> findByEntityTypeOrderByOccurredAtDesc(
      String entityType, Pageable pageable);
}
