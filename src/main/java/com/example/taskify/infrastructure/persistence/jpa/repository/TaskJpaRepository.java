package com.example.taskify.infrastructure.persistence.jpa.repository;

import com.example.taskify.infrastructure.persistence.jpa.entity.TaskJpaEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskJpaRepository extends JpaRepository<TaskJpaEntity, UUID> {
  List<TaskJpaEntity> findByProjectIdOrderByCreatedAtDesc(UUID projectId);
}
