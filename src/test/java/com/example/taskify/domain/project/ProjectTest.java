package com.example.taskify.domain.project;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ProjectTest {

  private static final UUID PROJECT_ID = UUID.fromString("12345678-1234-1234-1234-123456789012");
  private static final UUID OWNER_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
  private static final UUID CREATOR_ID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
  private static final Instant NOW = Instant.parse("2025-01-01T00:00:00Z");

  @Test
  void createInitialisesFields() {
    Project project =
        Project.create(
            PROJECT_ID,
            "  Acme Launch ",
            "Important project",
            OWNER_ID,
            null,
            LocalDate.of(2025, 2, 1),
            LocalDate.of(2025, 5, 31),
            CREATOR_ID,
            () -> NOW);

    assertThat(project.getId()).isEqualTo(PROJECT_ID);
    assertThat(project.getName()).isEqualTo("Acme Launch");
    assertThat(project.getDescription()).isEqualTo("Important project");
    assertThat(project.getOwnerId()).isEqualTo(OWNER_ID);
    assertThat(project.getStatus()).isEqualTo(ProjectStatus.PLANNED);
    assertThat(project.getStartDate()).contains(LocalDate.of(2025, 2, 1));
    assertThat(project.getEndDate()).contains(LocalDate.of(2025, 5, 31));
    assertThat(project.getCreatedBy()).isEqualTo(CREATOR_ID);
    assertThat(project.getCreatedAt()).isEqualTo(NOW);
  }

  @Test
  void changeStatusPublishesEvent() {
    Project project =
        Project.create(
            PROJECT_ID,
            "Acme Launch",
            "Important project",
            OWNER_ID,
            ProjectStatus.PLANNED,
            null,
            null,
            CREATOR_ID,
            () -> NOW);

    UUID actorId = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");
    Instant later = NOW.plusSeconds(30);

    project.changeStatus(ProjectStatus.ACTIVE, actorId, () -> later);
    List<?> events = project.drainEvents();

    assertThat(project.getStatus()).isEqualTo(ProjectStatus.ACTIVE);
    assertThat(project.getUpdatedBy()).contains(actorId);
    assertThat(project.getUpdatedAt()).isEqualTo(later);
    assertThat(events)
        .singleElement()
        .isInstanceOfSatisfying(
            ProjectStatusChangedEvent.class,
            event -> {
              assertThat(event.previousStatus()).isEqualTo(ProjectStatus.PLANNED);
              assertThat(event.newStatus()).isEqualTo(ProjectStatus.ACTIVE);
              assertThat(event.actorId()).isEqualTo(actorId);
            });
  }

  @Test
  void changeStatusRejectsInvalidTransition() {
    Project project =
        Project.create(
            PROJECT_ID,
            "Acme Launch",
            "Important project",
            OWNER_ID,
            ProjectStatus.PLANNED,
            null,
            null,
            CREATOR_ID,
            () -> NOW);

    UUID actorId = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd");

    project.changeStatus(ProjectStatus.ACTIVE, actorId, () -> NOW.plusSeconds(5));
    project.drainEvents();

    project.changeStatus(ProjectStatus.COMPLETED, actorId, () -> NOW.plusSeconds(10));
    project.drainEvents();

    assertThatThrownBy(
            () -> project.changeStatus(ProjectStatus.PLANNED, actorId, () -> NOW.plusSeconds(20)))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Cannot transition project");
  }
}
