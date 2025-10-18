package com.example.taskify.web;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.taskify.domain.activity.ActivityEntry;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class ActivityMapperTest {

  private final ActivityMapper mapper = Mappers.getMapper(ActivityMapper.class);

  @Test
  void toResponseCopiesFields() {
    ActivityEntry entry =
        new ActivityEntry(
            UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),
            "TASK",
            UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"),
            UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc"),
            "UPDATED",
            Map.of("field", "value"),
            Instant.parse("2024-01-01T00:00:00Z"));

    ActivityResponse response = mapper.toResponse(entry);

    assertThat(response.id()).isEqualTo(entry.id());
    assertThat(response.entityType()).isEqualTo("TASK");
    assertThat(response.payload()).containsEntry("field", "value");
  }

  @Test
  void toResponsesMapsList() {
    ActivityEntry entry =
        new ActivityEntry(
            UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd"),
            "PROJECT",
            UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee"),
            null,
            "CREATED",
            Map.of(),
            Instant.parse("2024-02-01T00:00:00Z"));

    List<ActivityResponse> responses = mapper.toResponses(List.of(entry));

    assertThat(responses).hasSize(1);
    assertThat(responses.get(0).entityType()).isEqualTo("PROJECT");
  }
}
