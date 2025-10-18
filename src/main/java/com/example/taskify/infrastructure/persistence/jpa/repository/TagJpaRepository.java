package com.example.taskify.infrastructure.persistence.jpa.repository;

import com.example.taskify.infrastructure.persistence.jpa.entity.TagJpaEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagJpaRepository extends JpaRepository<TagJpaEntity, UUID> {
  Optional<TagJpaEntity> findByNameIgnoreCase(String name);

  boolean existsByNameIgnoreCase(String name);
}
