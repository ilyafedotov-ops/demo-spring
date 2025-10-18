package com.example.taskify.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.taskify.application.service.ProjectService;
import com.example.taskify.config.security.SecurityConfig;
import com.example.taskify.domain.project.Project;
import com.example.taskify.domain.project.ProjectStatus;
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

@WebMvcTest(ProjectController.class)
@Import({SecurityConfig.class, ActorResolver.class, ApiErrorHandler.class})
class ProjectControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @MockBean private ProjectService projectService;
  @MockBean private ProjectMapper projectMapper;

  private static final UUID PROJECT_ID = UUID.fromString("11111111-2222-3333-4444-555555555555");
  private static final UUID OWNER_ID = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
  private static final UUID ACTOR_ID = UUID.fromString("99999999-8888-7777-6666-555555555555");

  @Test
  void createProjectReturnsCreated() throws Exception {
    ProjectCreateRequest request =
        new ProjectCreateRequest(
            "Project Falcon",
            "Bird project",
            OWNER_ID,
            ProjectStatus.PLANNED,
            LocalDate.of(2025, 1, 1),
            LocalDate.of(2025, 6, 30));
    Project domain = sampleProject();
    ProjectResponse response = sampleResponse();

    when(projectService.createProject(any(), any(), any(), any(), any(), any(), any()))
        .thenReturn(domain);
    when(projectMapper.toResponse(domain)).thenReturn(response);

    mockMvc
        .perform(
            authenticated(post("/api/projects"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", "/api/projects/" + PROJECT_ID))
        .andExpect(jsonPath("$.id").value(PROJECT_ID.toString()))
        .andExpect(jsonPath("$.name").value("Project Falcon"));

    verify(projectService)
        .createProject(
            eq("Project Falcon"),
            eq("Bird project"),
            eq(OWNER_ID),
            eq(ProjectStatus.PLANNED),
            eq(LocalDate.of(2025, 1, 1)),
            eq(LocalDate.of(2025, 6, 30)),
            eq(ACTOR_ID));
  }

  @Test
  void getProjectReturnsResponse() throws Exception {
    Project domain = sampleProject();
    ProjectResponse response = sampleResponse();

    when(projectService.getProject(PROJECT_ID)).thenReturn(domain);
    when(projectMapper.toResponse(domain)).thenReturn(response);

    mockMvc
        .perform(authenticated(get("/api/projects/{projectId}", PROJECT_ID)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(PROJECT_ID.toString()));

    verify(projectService).getProject(PROJECT_ID);
  }

  @Test
  void getProjectReturnsNotFoundWhenMissing() throws Exception {
    doThrow(new IllegalArgumentException("Project not found"))
        .when(projectService)
        .getProject(PROJECT_ID);

    mockMvc
        .perform(authenticated(get("/api/projects/{projectId}", PROJECT_ID)))
        .andExpect(status().isNotFound());
  }

  @Test
  void listProjectsReturnsResponses() throws Exception {
    Project domain = sampleProject();
    ProjectResponse response = sampleResponse();

    when(projectService.listProjects()).thenReturn(List.of(domain));
    when(projectMapper.toResponses(List.of(domain))).thenReturn(List.of(response));

    mockMvc
        .perform(authenticated(get("/api/projects")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(PROJECT_ID.toString()));
  }

  @Test
  void updateProjectReturnsResponse() throws Exception {
    ProjectUpdateRequest request =
        new ProjectUpdateRequest(
            "Updated Project",
            "Updated description",
            LocalDate.of(2025, 2, 1),
            LocalDate.of(2025, 7, 31));
    Project domain = sampleProject();
    ProjectResponse response = sampleResponse();

    when(projectService.updateDetails(eq(PROJECT_ID), any(), any(), eq(ACTOR_ID), any(), any()))
        .thenReturn(domain);
    when(projectMapper.toResponse(domain)).thenReturn(response);

    mockMvc
        .perform(
            authenticated(put("/api/projects/{projectId}", PROJECT_ID))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(PROJECT_ID.toString()))
        .andExpect(jsonPath("$.name").value("Project Falcon"));

    verify(projectService)
        .updateDetails(
            eq(PROJECT_ID),
            eq("Updated Project"),
            eq("Updated description"),
            eq(ACTOR_ID),
            eq(LocalDate.of(2025, 2, 1)),
            eq(LocalDate.of(2025, 7, 31)));
  }

  @Test
  void changeStatusReturnsResponse() throws Exception {
    ProjectStatusUpdateRequest request = new ProjectStatusUpdateRequest(ProjectStatus.ACTIVE);
    Project domain = sampleProject();
    ProjectResponse response = sampleResponse();

    when(projectService.changeStatus(PROJECT_ID, ProjectStatus.ACTIVE, ACTOR_ID))
        .thenReturn(domain);
    when(projectMapper.toResponse(domain)).thenReturn(response);

    mockMvc
        .perform(
            authenticated(patch("/api/projects/{projectId}/status", PROJECT_ID))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());

    verify(projectService).changeStatus(PROJECT_ID, ProjectStatus.ACTIVE, ACTOR_ID);
  }

  @Test
  void changeOwnerReturnsResponse() throws Exception {
    UUID newOwner = UUID.fromString("00000000-0000-0000-0000-000000000001");
    ProjectOwnerChangeRequest request = new ProjectOwnerChangeRequest(newOwner);
    Project domain = sampleProject();
    ProjectResponse response = sampleResponse();

    when(projectService.reassignOwner(PROJECT_ID, newOwner, ACTOR_ID)).thenReturn(domain);
    when(projectMapper.toResponse(domain)).thenReturn(response);

    mockMvc
        .perform(
            authenticated(patch("/api/projects/{projectId}/owner", PROJECT_ID))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());

    verify(projectService).reassignOwner(PROJECT_ID, newOwner, ACTOR_ID);
  }

  private MockHttpServletRequestBuilder authenticated(MockHttpServletRequestBuilder builder) {
    return builder.header("X-Actor-Id", ACTOR_ID.toString());
  }

  private Project sampleProject() {
    Instant now = Instant.parse("2025-01-01T00:00:00Z");
    return Project.create(
        PROJECT_ID,
        "Project Falcon",
        "Bird project",
        OWNER_ID,
        ProjectStatus.PLANNED,
        LocalDate.of(2025, 1, 1),
        LocalDate.of(2025, 6, 30),
        ACTOR_ID,
        () -> now);
  }

  private ProjectResponse sampleResponse() {
    Instant now = Instant.parse("2025-01-01T00:00:00Z");
    return new ProjectResponse(
        PROJECT_ID,
        "Project Falcon",
        "Bird project",
        OWNER_ID,
        ProjectStatus.PLANNED,
        LocalDate.of(2025, 1, 1),
        LocalDate.of(2025, 6, 30),
        ACTOR_ID,
        now,
        ACTOR_ID,
        now);
  }
}
