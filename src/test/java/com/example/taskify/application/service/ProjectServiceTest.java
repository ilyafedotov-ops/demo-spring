package com.example.taskify.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.taskify.application.port.out.ProjectEventPublisher;
import com.example.taskify.application.port.out.ProjectRepository;
import com.example.taskify.application.port.out.UserDirectory;
import com.example.taskify.domain.common.DomainEvent;
import com.example.taskify.domain.project.Project;
import com.example.taskify.domain.project.ProjectStatus;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

  private static final Instant NOW = Instant.parse("2025-01-01T00:00:00Z");
  private static final UUID PROJECT_ID = UUID.fromString("aaaaaaaa-0000-0000-0000-000000000000");
  private static final UUID OWNER_ID = UUID.fromString("bbbbbbbb-0000-0000-0000-000000000000");
  private static final UUID CREATOR_ID = UUID.fromString("cccccccc-0000-0000-0000-000000000000");
  private static final UUID ACTOR_ID = UUID.fromString("dddddddd-0000-0000-0000-000000000000");

  @Mock private ProjectRepository projectRepository;
  @Mock private ProjectEventPublisher eventPublisher;
  @Mock private UserDirectory userDirectory;

  private ProjectService projectService;
  private Supplier<UUID> uuidSupplier;

  @BeforeEach
  void setUp() {
    uuidSupplier = () -> PROJECT_ID;
    projectService =
        new ProjectService(
            projectRepository,
            eventPublisher,
            userDirectory,
            uuidSupplier,
            Clock.fixed(NOW, ZoneOffset.UTC));
  }

  @Test
  void createProjectValidatesOwner() {
    when(userDirectory.existsById(OWNER_ID)).thenReturn(true);
    when(projectRepository.save(any(Project.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    Project project =
        projectService.createProject(
            "Launch",
            "Description",
            OWNER_ID,
            ProjectStatus.PLANNED,
            LocalDate.of(2025, 2, 1),
            LocalDate.of(2025, 3, 1),
            CREATOR_ID);

    assertThat(project.getId()).isEqualTo(PROJECT_ID);
    verify(projectRepository).save(any(Project.class));
    ArgumentCaptor<Collection<DomainEvent>> eventsCaptor =
        ArgumentCaptor.forClass(Collection.class);
    verify(eventPublisher).publish(eventsCaptor.capture());
    assertThat(eventsCaptor.getValue()).isEmpty();
  }

  @Test
  void changeStatusEmitsEvent() {
    Project project =
        Project.create(
            PROJECT_ID,
            "Launch",
            "Description",
            OWNER_ID,
            ProjectStatus.PLANNED,
            null,
            null,
            CREATOR_ID,
            () -> NOW);
    when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));
    when(projectRepository.save(any(Project.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    projectService.changeStatus(PROJECT_ID, ProjectStatus.ACTIVE, ACTOR_ID);

    ArgumentCaptor<Collection<DomainEvent>> captor = ArgumentCaptor.forClass(Collection.class);
    verify(eventPublisher).publish(captor.capture());
    assertThat(captor.getValue()).isNotEmpty();
  }

  @Test
  void reassignOwnerValidatesUser() {
    Project project =
        Project.create(
            PROJECT_ID,
            "Launch",
            "Description",
            OWNER_ID,
            ProjectStatus.PLANNED,
            null,
            null,
            CREATOR_ID,
            () -> NOW);
    UUID newOwner = UUID.fromString("eeeeeeee-0000-0000-0000-000000000000");
    when(userDirectory.existsById(newOwner)).thenReturn(true);
    when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));
    when(projectRepository.save(any(Project.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    projectService.reassignOwner(PROJECT_ID, newOwner, ACTOR_ID);

    verify(userDirectory).existsById(newOwner);
    verify(projectRepository).save(any(Project.class));
  }

  @Test
  void reassignOwnerRejectsUnknownUser() {
    UUID newOwner = UUID.fromString("ffffffff-0000-0000-0000-000000000000");
    when(userDirectory.existsById(newOwner)).thenReturn(false);

    assertThatThrownBy(() -> projectService.reassignOwner(PROJECT_ID, newOwner, ACTOR_ID))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Owner does not exist");

    verify(projectRepository, never()).findById(any());
    verify(eventPublisher, never()).publish(any());
  }

  @Test
  void createProjectRejectsUnknownOwner() {
    when(userDirectory.existsById(OWNER_ID)).thenReturn(false);

    assertThatThrownBy(
            () ->
                projectService.createProject(
                    "Launch",
                    "Description",
                    OWNER_ID,
                    ProjectStatus.PLANNED,
                    null,
                    null,
                    CREATOR_ID))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Owner does not exist");

    verify(projectRepository, never()).save(any());
    verify(eventPublisher, never()).publish(any());
  }

  @Test
  void changeStatusThrowsWhenProjectMissing() {
    when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.empty());

    assertThatThrownBy(
            () -> projectService.changeStatus(PROJECT_ID, ProjectStatus.ACTIVE, ACTOR_ID))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Project not found");
  }

  @Test
  void updateDetailsRenamesAndSchedulesProject() {
    Project project =
        Project.create(
            PROJECT_ID,
            "Old name",
            "Old",
            OWNER_ID,
            ProjectStatus.PLANNED,
            null,
            null,
            CREATOR_ID,
            () -> NOW);
    when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));
    when(projectRepository.save(any(Project.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    Project updated =
        projectService.updateDetails(
            PROJECT_ID,
            "  New Name ",
            "Updated description",
            ACTOR_ID,
            LocalDate.of(2025, 4, 1),
            LocalDate.of(2025, 5, 1));

    assertThat(updated.getName()).isEqualTo("New Name");
    assertThat(updated.getDescription()).isEqualTo("Updated description");
    assertThat(updated.getStartDate()).hasValue(LocalDate.of(2025, 4, 1));
    assertThat(updated.getEndDate()).hasValue(LocalDate.of(2025, 5, 1));
    verify(eventPublisher).publish(any());
  }

  @Test
  void getProjectReturnsExistingProject() {
    Project project =
        Project.create(
            PROJECT_ID,
            "Launch",
            "Description",
            OWNER_ID,
            ProjectStatus.PLANNED,
            null,
            null,
            CREATOR_ID,
            () -> NOW);
    when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));

    Project result = projectService.getProject(PROJECT_ID);

    assertThat(result).isSameAs(project);
  }

  @Test
  void getProjectThrowsWhenMissing() {
    when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> projectService.getProject(PROJECT_ID))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Project not found");
  }

  @Test
  void listProjectsDelegatesToRepository() {
    Project project =
        Project.create(
            PROJECT_ID,
            "Launch",
            "Description",
            OWNER_ID,
            ProjectStatus.PLANNED,
            null,
            null,
            CREATOR_ID,
            () -> NOW);
    when(projectRepository.findAll()).thenReturn(List.of(project));

    assertThat(projectService.listProjects()).containsExactly(project);
  }
}
