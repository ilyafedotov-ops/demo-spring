package com.example.taskify.infrastructure.persistence.jpa.adapter;

import com.example.taskify.application.port.out.TaskRepository;
import com.example.taskify.domain.task.Task;
import com.example.taskify.infrastructure.persistence.jpa.entity.TaskJpaEntity;
import com.example.taskify.infrastructure.persistence.jpa.mapper.TaskEntityMapper;
import com.example.taskify.infrastructure.persistence.jpa.repository.TaskJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class TaskRepositoryAdapter implements TaskRepository {

  private final TaskJpaRepository jpaRepository;
  private final TaskEntityMapper mapper;

  public TaskRepositoryAdapter(TaskJpaRepository jpaRepository, TaskEntityMapper mapper) {
    this.jpaRepository = jpaRepository;
    this.mapper = mapper;
  }

  @Override
  public Task save(Task task) {
    TaskJpaEntity entity = mapper.toEntity(task);
    jpaRepository.save(entity);
    return task;
  }

  @Override
  public Optional<Task> findById(UUID id) {
    return jpaRepository.findById(id).map(mapper::toDomain);
  }

  @Override
  public List<Task> findByProjectId(UUID projectId) {
    return jpaRepository.findByProjectIdOrderByCreatedAtDesc(projectId).stream()
        .map(mapper::toDomain)
        .toList();
  }
}
