package com.example.taskify.infrastructure.persistence.jpa.adapter;

import com.example.taskify.application.port.out.TaskTagRepository;
import com.example.taskify.infrastructure.persistence.jpa.entity.TaskTagJpaEntity;
import com.example.taskify.infrastructure.persistence.jpa.repository.TaskTagJpaRepository;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TaskTagRepositoryAdapter implements TaskTagRepository {

  private final TaskTagJpaRepository taskTagJpaRepository;

  public TaskTagRepositoryAdapter(TaskTagJpaRepository taskTagJpaRepository) {
    this.taskTagJpaRepository = taskTagJpaRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public boolean isTagAttached(UUID taskId, UUID tagId) {
    Objects.requireNonNull(taskId, "taskId must not be null");
    Objects.requireNonNull(tagId, "tagId must not be null");
    return taskTagJpaRepository.existsByTaskIdAndTagId(taskId, tagId);
  }

  @Override
  @Transactional
  public void attach(UUID taskId, UUID tagId) {
    taskTagJpaRepository.save(new TaskTagJpaEntity(taskId, tagId));
  }

  @Override
  @Transactional
  public boolean detach(UUID taskId, UUID tagId) {
    return taskTagJpaRepository.deleteByTaskIdAndTagId(taskId, tagId) > 0;
  }

  @Override
  @Transactional(readOnly = true)
  public Set<UUID> listTagIds(UUID taskId) {
    Objects.requireNonNull(taskId, "taskId must not be null");
    return taskTagJpaRepository.findTagIdsByTaskId(taskId);
  }
}
