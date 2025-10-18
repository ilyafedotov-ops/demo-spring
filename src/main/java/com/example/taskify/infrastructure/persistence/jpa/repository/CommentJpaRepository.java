package com.example.taskify.infrastructure.persistence.jpa.repository;

import com.example.taskify.infrastructure.persistence.jpa.entity.CommentJpaEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentJpaRepository extends JpaRepository<CommentJpaEntity, UUID> {
  List<CommentJpaEntity> findByTaskIdOrderByCreatedAtAsc(UUID taskId);
}
