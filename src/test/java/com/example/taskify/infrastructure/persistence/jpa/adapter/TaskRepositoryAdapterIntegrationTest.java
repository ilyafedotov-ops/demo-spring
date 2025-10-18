package com.example.taskify.infrastructure.persistence.jpa.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.taskify.application.port.out.ProjectRepository;
import com.example.taskify.application.port.out.TaskRepository;
import com.example.taskify.domain.project.Project;
import com.example.taskify.domain.project.ProjectStatus;
import com.example.taskify.domain.task.Task;
import com.example.taskify.domain.task.TaskPriority;
import com.example.taskify.domain.task.TaskStatus;
import com.example.taskify.domain.user.UserRole;
import com.example.taskify.domain.user.UserStatus;
import com.example.taskify.infrastructure.persistence.jpa.entity.UserJpaEntity;
import com.example.taskify.infrastructure.persistence.jpa.mapper.ProjectEntityMapper;
import com.example.taskify.infrastructure.persistence.jpa.mapper.TaskEntityMapper;
import com.example.taskify.infrastructure.persistence.jpa.repository.TaskJpaRepository;
import com.example.taskify.infrastructure.persistence.jpa.repository.UserJpaRepository;
import com.example.taskify.support.PostgresIntegrationTest;
import java.time.Instant;
import java.time.LocalDate;
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
  TaskRepositoryAdapter.class,
  TaskEntityMapper.class,
  ProjectRepositoryAdapter.class,
  ProjectEntityMapper.class,
  DatabaseUserDirectory.class,
  com.example.taskify.config.TaskifyPropertiesConfiguration.class,
  com.example.taskify.config.PersistenceConfig.class
})
class TaskRepositoryAdapterIntegrationTest extends PostgresIntegrationTest {

  @Autowired private TaskRepository taskRepository;
  @Autowired private ProjectRepository projectRepository;
  @Autowired private UserJpaRepository userJpaRepository;
  @Autowired private TaskJpaRepository taskJpaRepository;

  private UUID ownerId;
  private UUID projectId;

  @BeforeEach
  void setUp() {
    ownerId = UUID.randomUUID();
    projectId = UUID.randomUUID();
    Instant now = Instant.now();

    UserJpaEntity user = new UserJpaEntity();
    user.setId(ownerId);
    user.setUsername("owner");
    user.setEmail("owner@example.com");
    user.setPasswordHash("hashed");
    user.setDisplayName("Owner");
    user.setRole(UserRole.TEAM_LEAD);
    user.setStatus(UserStatus.ACTIVE);
    user.setLastLoginAt(now);
    user.setCreatedAt(now);
    user.setCreatedBy(ownerId);
    user.setUpdatedAt(now);
    user.setUpdatedBy(ownerId);
    userJpaRepository.save(user);

    Project project =
        Project.create(
            projectId,
            "Roadmap",
            "Initial project",
            ownerId,
            ProjectStatus.PLANNED,
            LocalDate.of(2025, 1, 1),
            LocalDate.of(2025, 6, 30),
            ownerId,
            Instant::now);
    projectRepository.save(project);
  }

  @Test
  void saveAndFindTask() {
    UUID taskId = UUID.randomUUID();
    Instant now = Instant.now();

    Task task =
        Task.create(
            taskId,
            projectId,
            "Implement feature",
            "Description",
            TaskPriority.HIGH,
            TaskStatus.TODO,
            null,
            LocalDate.of(2025, 3, 15),
            ownerId,
            () -> now);

    Task saved = taskRepository.save(task);
    assertThat(saved).isSameAs(task);
    assertThat(taskJpaRepository.existsById(taskId)).isTrue();

    Task reloaded = taskRepository.findById(taskId).orElseThrow();
    assertThat(reloaded.getId()).isEqualTo(taskId);
    assertThat(reloaded.getTitle()).isEqualTo("Implement feature");
    assertThat(reloaded.getProjectId()).isEqualTo(projectId);
    assertThat(reloaded.getPriority()).isEqualTo(TaskPriority.HIGH);
    assertThat(reloaded.getStatus()).isEqualTo(TaskStatus.TODO);
    assertThat(reloaded.getCompletedAt()).isEmpty();
  }

  @Test
  void updatesPersistedTaskStatus() {
    UUID taskId = UUID.randomUUID();
    Task task =
        Task.create(
            taskId,
            projectId,
            "Review PR",
            null,
            TaskPriority.MEDIUM,
            TaskStatus.TODO,
            null,
            null,
            ownerId,
            Instant::now);
    taskRepository.save(task);

    task.changeStatus(TaskStatus.IN_PROGRESS, ownerId, Instant::now);
    taskRepository.save(task);

    Task persisted = taskRepository.findById(taskId).orElseThrow();
    assertThat(persisted.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
  }

  @Test
  void findByProjectIdReturnsTasksOrderedByCreationDescending() {
    Instant earlier = Instant.parse("2025-01-01T10:00:00Z");
    Instant later = Instant.parse("2025-01-02T12:00:00Z");
    Task first =
        Task.create(
            UUID.randomUUID(),
            projectId,
            "First task",
            null,
            TaskPriority.MEDIUM,
            TaskStatus.TODO,
            null,
            null,
            ownerId,
            () -> later);
    Task second =
        Task.create(
            UUID.randomUUID(),
            projectId,
            "Second task",
            null,
            TaskPriority.LOW,
            TaskStatus.TODO,
            null,
            null,
            ownerId,
            () -> earlier);
    taskRepository.save(first);
    taskRepository.save(second);

    var results = taskRepository.findByProjectId(projectId);

    assertThat(results).hasSize(2);
    assertThat(results.get(0).getCreatedAt()).isEqualTo(later);
    assertThat(results.get(1).getCreatedAt()).isEqualTo(earlier);
  }
}
