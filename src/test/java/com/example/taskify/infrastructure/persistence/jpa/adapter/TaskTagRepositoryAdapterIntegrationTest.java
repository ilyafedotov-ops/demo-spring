package com.example.taskify.infrastructure.persistence.jpa.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.taskify.application.port.out.ProjectRepository;
import com.example.taskify.application.port.out.TagRepository;
import com.example.taskify.application.port.out.TaskRepository;
import com.example.taskify.application.port.out.TaskTagRepository;
import com.example.taskify.domain.project.Project;
import com.example.taskify.domain.project.ProjectStatus;
import com.example.taskify.domain.tag.Tag;
import com.example.taskify.domain.task.Task;
import com.example.taskify.domain.task.TaskPriority;
import com.example.taskify.domain.task.TaskStatus;
import com.example.taskify.domain.user.UserRole;
import com.example.taskify.domain.user.UserStatus;
import com.example.taskify.infrastructure.persistence.jpa.entity.UserJpaEntity;
import com.example.taskify.infrastructure.persistence.jpa.mapper.ProjectEntityMapper;
import com.example.taskify.infrastructure.persistence.jpa.mapper.TagEntityMapper;
import com.example.taskify.infrastructure.persistence.jpa.mapper.TaskEntityMapper;
import com.example.taskify.infrastructure.persistence.jpa.repository.UserJpaRepository;
import com.example.taskify.support.PostgresIntegrationTest;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
  TaskTagRepositoryAdapter.class,
  TaskRepositoryAdapter.class,
  TaskEntityMapper.class,
  ProjectRepositoryAdapter.class,
  ProjectEntityMapper.class,
  TagRepositoryAdapter.class,
  TagEntityMapper.class,
  com.example.taskify.config.TaskifyPropertiesConfiguration.class,
  com.example.taskify.config.PersistenceConfig.class
})
class TaskTagRepositoryAdapterIntegrationTest extends PostgresIntegrationTest {

  @Autowired private TaskTagRepository taskTagRepository;
  @Autowired private TaskRepository taskRepository;
  @Autowired private ProjectRepository projectRepository;
  @Autowired private TagRepository tagRepository;
  @Autowired private UserJpaRepository userJpaRepository;

  private UUID projectId;
  private UUID ownerId;
  private UUID taskId;
  private UUID tagId;

  @BeforeEach
  void setUp() {
    Instant now = Instant.now();
    ownerId = UUID.randomUUID();
    projectId = UUID.randomUUID();
    taskId = UUID.randomUUID();
    tagId = UUID.randomUUID();

    UserJpaEntity owner = new UserJpaEntity();
    owner.setId(ownerId);
    owner.setUsername("owner");
    owner.setEmail("owner@example.com");
    owner.setPasswordHash("hashed");
    owner.setDisplayName("Owner");
    owner.setRole(UserRole.TEAM_LEAD);
    owner.setStatus(UserStatus.ACTIVE);
    owner.setLastLoginAt(now);
    owner.setCreatedAt(now);
    owner.setCreatedBy(ownerId);
    owner.setUpdatedAt(now);
    owner.setUpdatedBy(ownerId);
    userJpaRepository.save(owner);

    Project project =
        Project.create(
            projectId,
            "Roadmap",
            "Initial scope",
            ownerId,
            ProjectStatus.PLANNED,
            LocalDate.of(2025, 1, 1),
            LocalDate.of(2025, 6, 30),
            ownerId,
            Instant::now);
    projectRepository.save(project);

    Task task =
        Task.create(
            taskId,
            projectId,
            "Implement tagging",
            null,
            TaskPriority.MEDIUM,
            TaskStatus.TODO,
            null,
            null,
            ownerId,
            Instant::now);
    taskRepository.save(task);

    Tag tag = Tag.create(tagId, "Backend", "#2563EB", now);
    tagRepository.save(tag);
  }

  @Test
  void attachAndDetachTagMaintainsJoinTable() {
    assertThat(taskTagRepository.isTagAttached(taskId, tagId)).isFalse();

    taskTagRepository.attach(taskId, tagId);

    assertThat(taskTagRepository.isTagAttached(taskId, tagId)).isTrue();
    Set<UUID> tagIds = taskTagRepository.listTagIds(taskId);
    assertThat(tagIds).containsExactly(tagId);

    boolean removed = taskTagRepository.detach(taskId, tagId);
    assertThat(removed).isTrue();
    assertThat(taskTagRepository.isTagAttached(taskId, tagId)).isFalse();
  }

  @Test
  void detachReturnsFalseWhenAssociationMissing() {
    assertThat(taskTagRepository.detach(taskId, tagId)).isFalse();
  }
}
