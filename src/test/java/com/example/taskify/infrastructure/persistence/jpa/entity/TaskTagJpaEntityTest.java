package com.example.taskify.infrastructure.persistence.jpa.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class TaskTagJpaEntityTest {

  @Test
  void constructorsAndAccessorsAreConsistent() {
    UUID taskId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    UUID tagId = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

    TaskTagJpaEntity entity = new TaskTagJpaEntity(taskId, tagId);
    assertThat(entity.getTaskId()).isEqualTo(taskId);
    assertThat(entity.getTagId()).isEqualTo(tagId);

    UUID newTaskId = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");
    UUID newTagId = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd");
    entity.setTaskId(newTaskId);
    entity.setTagId(newTagId);

    assertThat(entity.getTaskId()).isEqualTo(newTaskId);
    assertThat(entity.getTagId()).isEqualTo(newTagId);
  }

  @Test
  void taskTagIdValueObjectImplementsEquality() {
    UUID taskId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    UUID tagId = UUID.fromString("22222222-2222-2222-2222-222222222222");

    TaskTagJpaEntity.TaskTagId first = new TaskTagJpaEntity.TaskTagId(taskId, tagId);
    TaskTagJpaEntity.TaskTagId second = new TaskTagJpaEntity.TaskTagId(taskId, tagId);
    TaskTagJpaEntity.TaskTagId different =
        new TaskTagJpaEntity.TaskTagId(
            UUID.fromString("33333333-3333-3333-3333-333333333333"),
            UUID.fromString("44444444-4444-4444-4444-444444444444"));

    assertThat(first).isEqualTo(second);
    assertThat(first).hasSameHashCodeAs(second);
    assertThat(first).isNotEqualTo(different);
  }
}
