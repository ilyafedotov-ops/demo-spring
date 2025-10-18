package com.example.taskify.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.taskify.application.port.out.ActivityLogRepository;
import com.example.taskify.application.port.out.ProjectRepository;
import com.example.taskify.application.port.out.TagRepository;
import com.example.taskify.application.port.out.TaskRepository;
import com.example.taskify.application.port.out.TaskTagRepository;
import com.example.taskify.application.service.ActivityCodebook;
import com.example.taskify.domain.activity.ActivityEntry;
import com.example.taskify.domain.project.Project;
import com.example.taskify.domain.project.ProjectStatus;
import com.example.taskify.domain.tag.Tag;
import com.example.taskify.domain.task.Task;
import com.example.taskify.domain.task.TaskPriority;
import com.example.taskify.domain.task.TaskStatus;
import com.example.taskify.domain.user.UserRole;
import com.example.taskify.domain.user.UserStatus;
import com.example.taskify.infrastructure.persistence.jpa.entity.UserJpaEntity;
import com.example.taskify.infrastructure.persistence.jpa.repository.UserJpaRepository;
import com.example.taskify.support.PostgresIntegrationTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class TaskControllerIntegrationTest extends PostgresIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private TaskRepository taskRepository;
  @Autowired private ProjectRepository projectRepository;
  @Autowired private TagRepository tagRepository;
  @Autowired private TaskTagRepository taskTagRepository;
  @Autowired private ActivityLogRepository activityLogRepository;
  @Autowired private UserJpaRepository userJpaRepository;
  @Autowired private ObjectMapper objectMapper;

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

    persistUser(now);
    persistProject(now);
    persistTask(now);
    persistTag(now);
  }

  @Test
  void attachThenDetachTagRecordsActivityAndUpdatesJoinTable() throws Exception {
    mockMvc
        .perform(
            post("/api/tasks/{taskId}/tags/{tagId}", taskId, tagId)
                .header("X-Actor-Id", ownerId.toString())
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());

    assertThat(taskTagRepository.isTagAttached(taskId, tagId)).isTrue();
    List<ActivityEntry> afterAttach =
        activityLogRepository.findByEntity(taskId, ActivityCodebook.ENTITY_TASK);
    assertThat(afterAttach)
        .anyMatch(entry -> ActivityCodebook.ACTION_TASK_TAGGED.equals(entry.action()));

    mockMvc
        .perform(
            delete("/api/tasks/{taskId}/tags/{tagId}", taskId, tagId)
                .header("X-Actor-Id", ownerId.toString()))
        .andExpect(status().isNoContent());

    assertThat(taskTagRepository.isTagAttached(taskId, tagId)).isFalse();
    List<ActivityEntry> afterDetach =
        activityLogRepository.findByEntity(taskId, ActivityCodebook.ENTITY_TASK);
    assertThat(afterDetach)
        .anyMatch(entry -> ActivityCodebook.ACTION_TASK_UNTAGGED.equals(entry.action()));
  }

  @Test
  void getTaskReturnsTaskDetails() throws Exception {
    mockMvc
        .perform(get("/api/tasks/{taskId}", taskId).header("X-Actor-Id", ownerId.toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(taskId.toString()))
        .andExpect(jsonPath("$.title").value("Implement tagging"));
  }

  @Test
  void listTasksByProjectReturnsArray() throws Exception {
    mockMvc
        .perform(
            get("/api/tasks")
                .param("projectId", projectId.toString())
                .header("X-Actor-Id", ownerId.toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(taskId.toString()));
  }

  @Test
  void createTaskPersistsAndReturnsPayload() throws Exception {
    TaskCreateRequest request =
        new TaskCreateRequest(
            projectId,
            "Write docs",
            "Document tagging flow",
            TaskPriority.LOW,
            TaskStatus.TODO,
            null,
            LocalDate.of(2025, 4, 1));

    MvcResult result =
        mockMvc
            .perform(
                post("/api/tasks")
                    .header("X-Actor-Id", ownerId.toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.title").value("Write docs"))
            .andReturn();

    TaskResponse response =
        objectMapper.readValue(result.getResponse().getContentAsString(), TaskResponse.class);

    assertThat(taskRepository.findById(response.id())).isPresent();
  }

  private void persistUser(Instant now) {
    UserJpaEntity user = new UserJpaEntity();
    user.setId(ownerId);
    user.setUsername("owner-" + ownerId);
    user.setEmail("owner+" + ownerId + "@example.com");
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
  }

  private void persistProject(Instant now) {
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
            () -> now);
    projectRepository.save(project);
  }

  private void persistTask(Instant now) {
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
            () -> now);
    taskRepository.save(task);
  }

  private void persistTag(Instant now) {
    Tag tag = Tag.create(tagId, "Backend-" + tagId.toString().substring(0, 8), "#2563EB", now);
    tagRepository.save(tag);
  }
}
