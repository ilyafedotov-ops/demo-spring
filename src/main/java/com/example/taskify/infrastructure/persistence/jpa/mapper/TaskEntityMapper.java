package com.example.taskify.infrastructure.persistence.jpa.mapper;

import com.example.taskify.domain.task.Task;
import com.example.taskify.infrastructure.persistence.jpa.entity.TaskJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class TaskEntityMapper {

  public TaskJpaEntity toEntity(Task task) {
    TaskJpaEntity entity = new TaskJpaEntity();
    entity.setId(task.getId());
    entity.setProjectId(task.getProjectId());
    entity.setTitle(task.getTitle());
    entity.setDescription(task.getDescription());
    entity.setPriority(task.getPriority());
    entity.setStatus(task.getStatus());
    entity.setAssigneeId(task.getAssigneeId().orElse(null));
    entity.setDueDate(task.getDueDate().orElse(null));
    entity.setCompletedAt(task.getCompletedAt().orElse(null));
    entity.setCreatedAt(task.getCreatedAt());
    entity.setCreatedBy(task.getCreatedBy());
    entity.setUpdatedAt(task.getUpdatedAt());
    entity.setUpdatedBy(task.getUpdatedBy().orElse(null));
    return entity;
  }

  public Task toDomain(TaskJpaEntity entity) {
    return Task.rehydrate(
        entity.getId(),
        entity.getProjectId(),
        entity.getTitle(),
        entity.getDescription(),
        entity.getPriority(),
        entity.getStatus(),
        entity.getAssigneeId(),
        entity.getDueDate(),
        entity.getCompletedAt(),
        entity.getCreatedBy(),
        entity.getCreatedAt(),
        entity.getUpdatedBy(),
        entity.getUpdatedAt());
  }
}
