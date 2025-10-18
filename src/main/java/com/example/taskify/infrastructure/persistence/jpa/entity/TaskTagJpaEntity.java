package com.example.taskify.infrastructure.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "task_tags")
@IdClass(TaskTagJpaEntity.TaskTagId.class)
public class TaskTagJpaEntity {

  @Id
  @Column(name = "task_id", nullable = false)
  private UUID taskId;

  @Id
  @Column(name = "tag_id", nullable = false)
  private UUID tagId;

  public TaskTagJpaEntity() {}

  public TaskTagJpaEntity(UUID taskId, UUID tagId) {
    this.taskId = taskId;
    this.tagId = tagId;
  }

  public UUID getTaskId() {
    return taskId;
  }

  public UUID getTagId() {
    return tagId;
  }

  public void setTaskId(UUID taskId) {
    this.taskId = taskId;
  }

  public void setTagId(UUID tagId) {
    this.tagId = tagId;
  }

  public static class TaskTagId implements Serializable {
    private UUID taskId;
    private UUID tagId;

    public TaskTagId() {}

    public TaskTagId(UUID taskId, UUID tagId) {
      this.taskId = taskId;
      this.tagId = tagId;
    }

    public UUID getTaskId() {
      return taskId;
    }

    public UUID getTagId() {
      return tagId;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof TaskTagId other)) {
        return false;
      }
      return Objects.equals(taskId, other.taskId) && Objects.equals(tagId, other.tagId);
    }

    @Override
    public int hashCode() {
      return Objects.hash(taskId, tagId);
    }
  }
}
