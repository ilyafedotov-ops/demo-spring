package com.example.taskify.web;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.taskify.domain.project.Project;
import com.example.taskify.domain.project.ProjectStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class ProjectMapperTest {

  private final ProjectMapper mapper = Mappers.getMapper(ProjectMapper.class);

  @Test
  void toResponseConvertsOptionalFields() {
    Project project =
        Project.rehydrate(
            UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),
            "Project",
            "Description",
            UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"),
            ProjectStatus.ACTIVE,
            LocalDate.of(2024, 1, 1),
            LocalDate.of(2024, 2, 1),
            UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc"),
            Instant.parse("2024-01-01T00:00:00Z"),
            UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd"),
            Instant.parse("2024-01-02T00:00:00Z"));

    ProjectResponse response = mapper.toResponse(project);

    assertThat(response.id()).isEqualTo(project.getId());
    assertThat(response.startDate()).isEqualTo(LocalDate.of(2024, 1, 1));
    assertThat(response.updatedBy())
        .isEqualTo(UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd"));
  }

  @Test
  void toResponsesMapsCollection() {
    Project project =
        Project.rehydrate(
            UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee"),
            "Another",
            "Desc",
            UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff"),
            ProjectStatus.PLANNED,
            null,
            null,
            UUID.fromString("11111111-1111-1111-1111-111111111111"),
            Instant.parse("2024-03-01T00:00:00Z"),
            null,
            Instant.parse("2024-03-02T00:00:00Z"));

    List<ProjectResponse> responses = mapper.toResponses(List.of(project));

    assertThat(responses).hasSize(1);
    assertThat(responses.get(0).name()).isEqualTo("Another");
    assertThat(responses.get(0).startDate()).isNull();
  }
}
