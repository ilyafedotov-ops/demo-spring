package com.example.taskify.infrastructure.persistence.jpa.mapper;

import com.example.taskify.domain.project.Project;
import com.example.taskify.infrastructure.persistence.jpa.entity.ProjectJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class ProjectEntityMapper {

  public ProjectJpaEntity toEntity(Project project) {
    ProjectJpaEntity entity = new ProjectJpaEntity();
    entity.setId(project.getId());
    entity.setName(project.getName());
    entity.setDescription(project.getDescription());
    entity.setOwnerId(project.getOwnerId());
    entity.setStatus(project.getStatus());
    entity.setStartDate(project.getStartDate().orElse(null));
    entity.setEndDate(project.getEndDate().orElse(null));
    entity.setCreatedAt(project.getCreatedAt());
    entity.setCreatedBy(project.getCreatedBy());
    entity.setUpdatedAt(project.getUpdatedAt());
    entity.setUpdatedBy(project.getUpdatedBy().orElse(null));
    return entity;
  }

  public Project toDomain(ProjectJpaEntity entity) {
    return Project.rehydrate(
        entity.getId(),
        entity.getName(),
        entity.getDescription(),
        entity.getOwnerId(),
        entity.getStatus(),
        entity.getStartDate(),
        entity.getEndDate(),
        entity.getCreatedBy(),
        entity.getCreatedAt(),
        entity.getUpdatedBy(),
        entity.getUpdatedAt());
  }
}
