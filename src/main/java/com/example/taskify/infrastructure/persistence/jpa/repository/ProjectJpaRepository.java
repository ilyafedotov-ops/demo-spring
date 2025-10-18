package com.example.taskify.infrastructure.persistence.jpa.repository;

import com.example.taskify.infrastructure.persistence.jpa.entity.ProjectJpaEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectJpaRepository extends JpaRepository<ProjectJpaEntity, UUID> {}
