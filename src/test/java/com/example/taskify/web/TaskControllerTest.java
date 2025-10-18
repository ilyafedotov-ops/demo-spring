package com.example.taskify.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.taskify.application.exception.ResourceNotFoundException;
import com.example.taskify.application.service.TaskService;
import com.example.taskify.config.security.SecurityConfig;
import com.example.taskify.domain.task.Task;
import com.example.taskify.domain.task.TaskPriority;
import com.example.taskify.domain.task.TaskStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@WebMvcTest(TaskController.class)
@Import({SecurityConfig.class, ActorResolver.class, ApiErrorHandler.class})
class TaskControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockBean private TaskService taskService;
  @MockBean private TaskMapper taskMapper;

  private static final UUID TASK_ID = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
  private static final UUID TAG_ID = UUID.fromString("11111111-2222-3333-4444-555555555555");
  private static final UUID PROJECT_ID = UUID.fromString("99999999-8888-7777-6666-555555555555");
  private static final UUID ACTOR_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final UUID ASSIGNEE_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

  @Test
  void createTaskReturnsCreated() throws Exception {
    TaskCreateRequest request =
        new TaskCreateRequest(
            PROJECT_ID,
            "Title",
            "Description",
            TaskPriority.HIGH,
            TaskStatus.TODO,
            null,
            LocalDate.of(2025, 3, 1));
    Task domainTask =
        Task.create(
            TASK_ID,
            PROJECT_ID,
            "Title",
            "Description",
            TaskPriority.HIGH,
            TaskStatus.TODO,
            null,
            LocalDate.of(2025, 3, 1),
            ACTOR_ID,
            () -> Instant.parse("2025-01-01T00:00:00Z"));
    TaskResponse response = sampleResponse();

    when(taskService.createTask(any(), any(), any(), any(), any(), any(), any(), any()))
        .thenReturn(domainTask);
    when(taskMapper.toResponse(domainTask)).thenReturn(response);

    mockMvc
        .perform(
            authenticated(post("/api/tasks"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", "/api/tasks/" + TASK_ID))
        .andExpect(jsonPath("$.id").value(TASK_ID.toString()))
        .andExpect(jsonPath("$.title").value("Title"));

    verify(taskService)
        .createTask(
            eq(PROJECT_ID),
            eq("Title"),
            eq("Description"),
            eq(TaskPriority.HIGH),
            eq(TaskStatus.TODO),
            isNull(),
            eq(LocalDate.of(2025, 3, 1)),
            eq(ACTOR_ID));
  }

  @Test
  void getTaskReturnsResponse() throws Exception {
    Task domainTask =
        Task.create(
            TASK_ID,
            PROJECT_ID,
            "Title",
            "Description",
            TaskPriority.HIGH,
            TaskStatus.TODO,
            ASSIGNEE_ID,
            LocalDate.of(2025, 3, 1),
            ACTOR_ID,
            () -> Instant.parse("2025-01-02T00:00:00Z"));
    TaskResponse response = sampleResponse();

    when(taskService.getTask(TASK_ID)).thenReturn(domainTask);
    when(taskMapper.toResponse(domainTask)).thenReturn(response);

    mockMvc
        .perform(authenticated(get("/api/tasks/{taskId}", TASK_ID)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(TASK_ID.toString()))
        .andExpect(jsonPath("$.projectId").value(PROJECT_ID.toString()));

    verify(taskService).getTask(TASK_ID);
  }

  @Test
  void listTasksReturnsResponses() throws Exception {
    Task domainTask =
        Task.create(
            TASK_ID,
            PROJECT_ID,
            "Title",
            null,
            TaskPriority.MEDIUM,
            TaskStatus.TODO,
            null,
            null,
            ACTOR_ID,
            () -> Instant.parse("2025-01-03T00:00:00Z"));
    TaskResponse response = sampleResponse();

    when(taskService.listTasks(PROJECT_ID)).thenReturn(List.of(domainTask));
    when(taskMapper.toResponses(List.of(domainTask))).thenReturn(List.of(response));

    mockMvc
        .perform(authenticated(get("/api/tasks").param("projectId", PROJECT_ID.toString())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(TASK_ID.toString()));

    verify(taskService).listTasks(PROJECT_ID);
  }

  @Test
  void updateDetailsReturnsResponse() throws Exception {
    TaskUpdateDetailsRequest request =
        new TaskUpdateDetailsRequest("New title", "New description", TaskPriority.LOW, null);
    Task domainTask =
        Task.create(
            TASK_ID,
            PROJECT_ID,
            "New title",
            "New description",
            TaskPriority.LOW,
            TaskStatus.TODO,
            null,
            null,
            ACTOR_ID,
            () -> Instant.parse("2025-01-04T00:00:00Z"));
    TaskResponse response = sampleResponse();

    when(taskService.updateDetails(eq(TASK_ID), any(), any(), any(), any(), eq(ACTOR_ID)))
        .thenReturn(domainTask);
    when(taskMapper.toResponse(domainTask)).thenReturn(response);

    mockMvc
        .perform(
            authenticated(put("/api/tasks/{taskId}", TASK_ID))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(TASK_ID.toString()));

    verify(taskService)
        .updateDetails(
            eq(TASK_ID),
            eq("New title"),
            eq("New description"),
            eq(TaskPriority.LOW),
            isNull(),
            eq(ACTOR_ID));
  }

  @Test
  void assignTaskReturnsResponse() throws Exception {
    TaskAssignRequest request = new TaskAssignRequest(ASSIGNEE_ID);
    Task domainTask =
        Task.create(
            TASK_ID,
            PROJECT_ID,
            "Title",
            null,
            TaskPriority.MEDIUM,
            TaskStatus.TODO,
            ASSIGNEE_ID,
            null,
            ACTOR_ID,
            () -> Instant.parse("2025-01-05T00:00:00Z"));
    TaskResponse response = sampleResponse();

    when(taskService.assignTask(TASK_ID, ASSIGNEE_ID, ACTOR_ID)).thenReturn(domainTask);
    when(taskMapper.toResponse(domainTask)).thenReturn(response);

    mockMvc
        .perform(
            authenticated(patch("/api/tasks/{taskId}/assignee", TASK_ID))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(TASK_ID.toString()));

    verify(taskService).assignTask(TASK_ID, ASSIGNEE_ID, ACTOR_ID);
  }

  @Test
  void assignTaskReturnsBadRequestWhenValidationFails() throws Exception {
    TaskAssignRequest request = new TaskAssignRequest(ASSIGNEE_ID);

    doThrow(new ResourceNotFoundException("Assignee does not exist"))
        .when(taskService)
        .assignTask(TASK_ID, ASSIGNEE_ID, ACTOR_ID);

    mockMvc
        .perform(
            authenticated(patch("/api/tasks/{taskId}/assignee", TASK_ID))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNotFound());
  }

  @Test
  void changeStatusReturnsConflictWhenTransitionInvalid() throws Exception {
    TaskStatusUpdateRequest request = new TaskStatusUpdateRequest(TaskStatus.TODO);

    doThrow(new IllegalStateException("Cannot transition"))
        .when(taskService)
        .changeStatus(TASK_ID, TaskStatus.TODO, ACTOR_ID);

    mockMvc
        .perform(
            authenticated(patch("/api/tasks/{taskId}/status", TASK_ID))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isConflict());
  }

  @Test
  void attachTagReturnsNoContent() throws Exception {
    mockMvc
        .perform(
            authenticated(post("/api/tasks/{taskId}/tags/{tagId}", TASK_ID, TAG_ID))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());

    verify(taskService).attachTag(eq(TASK_ID), eq(TAG_ID), eq(ACTOR_ID));
    verifyNoMoreInteractions(taskService);
  }

  @Test
  void attachTagReturnsNotFoundWhenServiceThrows() throws Exception {
    doThrow(new ResourceNotFoundException("Task not found"))
        .when(taskService)
        .attachTag(TASK_ID, TAG_ID, ACTOR_ID);

    mockMvc
        .perform(authenticated(post("/api/tasks/{taskId}/tags/{tagId}", TASK_ID, TAG_ID)))
        .andExpect(status().isNotFound());

    verify(taskService).attachTag(eq(TASK_ID), eq(TAG_ID), eq(ACTOR_ID));
  }

  @Test
  void detachTagReturnsNoContent() throws Exception {
    mockMvc
        .perform(authenticated(delete("/api/tasks/{taskId}/tags/{tagId}", TASK_ID, TAG_ID)))
        .andExpect(status().isNoContent());

    verify(taskService).detachTag(eq(TASK_ID), eq(TAG_ID), eq(ACTOR_ID));
    verifyNoMoreInteractions(taskService);
  }

  @Test
  void detachTagReturnsNotFoundWhenServiceThrows() throws Exception {
    doThrow(new ResourceNotFoundException("Tag not found"))
        .when(taskService)
        .detachTag(TASK_ID, TAG_ID, ACTOR_ID);

    mockMvc
        .perform(authenticated(delete("/api/tasks/{taskId}/tags/{tagId}", TASK_ID, TAG_ID)))
        .andExpect(status().isNotFound());

    verify(taskService).detachTag(eq(TASK_ID), eq(TAG_ID), eq(ACTOR_ID));
  }

  @Test
  void missingActorHeaderReturnsUnauthorized() throws Exception {
    mockMvc
        .perform(
            post("/api/tasks/{taskId}/tags/{tagId}", TASK_ID, TAG_ID)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());

    verifyNoInteractions(taskService);
  }

  private TaskResponse sampleResponse() {
    Instant timestamp = Instant.parse("2025-01-10T00:00:00Z");
    return new TaskResponse(
        TASK_ID,
        PROJECT_ID,
        "Title",
        "Description",
        TaskPriority.HIGH,
        TaskStatus.TODO,
        ASSIGNEE_ID,
        LocalDate.of(2025, 3, 1),
        null,
        ACTOR_ID,
        timestamp,
        ACTOR_ID,
        timestamp);
  }

  private MockHttpServletRequestBuilder authenticated(MockHttpServletRequestBuilder builder) {
    return builder.header("X-Actor-Id", ACTOR_ID.toString());
  }
}
