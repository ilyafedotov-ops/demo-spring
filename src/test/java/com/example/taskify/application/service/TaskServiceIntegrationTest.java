package com.example.taskify.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.taskify.application.port.out.ActivityLogRepository;
import com.example.taskify.application.port.out.ProjectRepository;
import com.example.taskify.application.port.out.TaskRepository;
import com.example.taskify.domain.activity.ActivityEntry;
import com.example.taskify.domain.project.Project;
import com.example.taskify.domain.project.ProjectStatus;
import com.example.taskify.domain.task.Task;
import com.example.taskify.domain.task.TaskPriority;
import com.example.taskify.domain.task.TaskStatus;
import com.example.taskify.domain.user.UserRole;
import com.example.taskify.domain.user.UserStatus;
import com.example.taskify.infrastructure.persistence.jpa.entity.UserJpaEntity;
import com.example.taskify.infrastructure.persistence.jpa.repository.UserJpaRepository;
import com.example.taskify.support.PostgresIntegrationTest;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class TaskServiceIntegrationTest extends PostgresIntegrationTest {

  @Autowired private TaskService taskService;
  @Autowired private ProjectRepository projectRepository;
  @Autowired private TaskRepository taskRepository;
  @Autowired private ActivityLogRepository activityLogRepository;
  @Autowired private UserJpaRepository userJpaRepository;

  private UUID projectId;
  private UUID creatorId;
  private UUID actorId;
  private UUID assigneeId;

  @BeforeEach
  void setUp() {
    projectId = UUID.randomUUID();
    creatorId = UUID.randomUUID();
    actorId = UUID.randomUUID();
    assigneeId = UUID.randomUUID();

    Instant now = Instant.now();
    persistUser(creatorId, "creator", now);
    persistUser(actorId, "actor", now);
    persistUser(assigneeId, "assignee", now);
    Project project =
        Project.create(
            projectId,
            "Task Lifecycle Project",
            "Covers activity emission",
            creatorId,
            ProjectStatus.ACTIVE,
            LocalDate.now(),
            LocalDate.now().plusMonths(2),
            creatorId,
            Instant::now);
    projectRepository.save(project);
  }

  @Test
  void taskLifecycleOperationsRecordActivities() {
    Task createdTask =
        taskService.createTask(
            projectId,
            "Initial Task",
            "Seed description",
            TaskPriority.MEDIUM,
            TaskStatus.TODO,
            null,
            LocalDate.now().plusDays(5),
            creatorId);

    List<ActivityEntry> entries =
        activityLogRepository.findByEntity(createdTask.getId(), ActivityCodebook.ENTITY_TASK);
    assertThat(entries).hasSize(1);
    ActivityEntry createdEntry = entries.getFirst();
    assertThat(createdEntry.action()).isEqualTo(ActivityCodebook.ACTION_TASK_CREATED);
    assertThat(createdEntry.payload())
        .containsEntry("title", "Initial Task")
        .containsEntry("status", TaskStatus.TODO.name());
    assertThat(createdEntry.payload().get("projectId")).isEqualTo(projectId.toString());

    taskService.changeStatus(createdTask.getId(), TaskStatus.IN_PROGRESS, actorId);
    entries = activityLogRepository.findByEntity(createdTask.getId(), ActivityCodebook.ENTITY_TASK);
    ActivityEntry statusEntry =
        entries.stream()
            .filter(entry -> ActivityCodebook.ACTION_TASK_STATUS_CHANGED.equals(entry.action()))
            .reduce((first, second) -> second)
            .orElseThrow();
    assertThat(statusEntry.payload())
        .containsEntry("from", TaskStatus.TODO.name())
        .containsEntry("to", TaskStatus.IN_PROGRESS.name());

    taskService.assignTask(createdTask.getId(), assigneeId, actorId);
    entries = activityLogRepository.findByEntity(createdTask.getId(), ActivityCodebook.ENTITY_TASK);
    ActivityEntry assignmentEntry =
        entries.stream()
            .filter(entry -> ActivityCodebook.ACTION_TASK_ASSIGNED.equals(entry.action()))
            .reduce((first, second) -> second)
            .orElseThrow();
    assertThat(assignmentEntry.payload())
        .containsEntry("from", null)
        .containsEntry("to", assigneeId.toString());

    taskService.updateDetails(
        createdTask.getId(),
        "Retitled Task",
        "Updated description",
        TaskPriority.HIGH,
        null,
        actorId);
    entries = activityLogRepository.findByEntity(createdTask.getId(), ActivityCodebook.ENTITY_TASK);
    ActivityEntry updateEntry =
        entries.stream()
            .filter(entry -> ActivityCodebook.ACTION_TASK_UPDATED.equals(entry.action()))
            .reduce((first, second) -> second)
            .orElseThrow();
    assertThat(updateEntry.payload())
        .containsEntry("title", "Retitled Task")
        .containsEntry("priority", TaskPriority.HIGH.name())
        .containsEntry("dueDate", null);

    assertThat(entries)
        .extracting(ActivityEntry::action)
        .contains(
            ActivityCodebook.ACTION_TASK_CREATED,
            ActivityCodebook.ACTION_TASK_STATUS_CHANGED,
            ActivityCodebook.ACTION_TASK_ASSIGNED,
            ActivityCodebook.ACTION_TASK_UPDATED);
  }

  private void persistUser(UUID userId, String username, Instant now) {
    UserJpaEntity user = new UserJpaEntity();
    user.setId(userId);
    user.setUsername(username);
    user.setEmail(username + "@example.com");
    user.setPasswordHash("hashed");
    user.setDisplayName(username);
    user.setRole(UserRole.TEAM_MEMBER);
    user.setStatus(UserStatus.ACTIVE);
    user.setLastLoginAt(now);
    user.setCreatedAt(now);
    user.setCreatedBy(userId);
    user.setUpdatedAt(now);
    user.setUpdatedBy(userId);
    userJpaRepository.save(user);
  }
}
