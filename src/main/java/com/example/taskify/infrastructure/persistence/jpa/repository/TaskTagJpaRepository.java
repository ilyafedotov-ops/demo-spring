package com.example.taskify.infrastructure.persistence.jpa.repository;

import com.example.taskify.infrastructure.persistence.jpa.entity.TaskTagJpaEntity;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaskTagJpaRepository
    extends JpaRepository<TaskTagJpaEntity, TaskTagJpaEntity.TaskTagId> {

  boolean existsByTaskIdAndTagId(UUID taskId, UUID tagId);

  @Modifying
  @Query("delete from TaskTagJpaEntity tt where tt.taskId = :taskId and tt.tagId = :tagId")
  int deleteByTaskIdAndTagId(@Param("taskId") UUID taskId, @Param("tagId") UUID tagId);

  @Query("select tt.tagId from TaskTagJpaEntity tt where tt.taskId = :taskId")
  Set<UUID> findTagIdsByTaskId(@Param("taskId") UUID taskId);
}
