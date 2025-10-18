package com.example.taskify.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.taskify.application.port.out.ActivityLogRepository;
import com.example.taskify.domain.activity.ActivityEntry;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ActivityQueryServiceTest {

  @Mock private ActivityLogRepository activityLogRepository;

  private ActivityQueryService service;

  @BeforeEach
  void setUp() {
    service = new ActivityQueryService(activityLogRepository);
  }

  @Test
  void listByEntityReturnsActivities() {
    UUID entityId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    ActivityEntry entry =
        new ActivityEntry(
            UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"),
            "TASK",
            entityId,
            UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc"),
            "UPDATED",
            null,
            Instant.parse("2024-01-01T00:00:00Z"));
    when(activityLogRepository.findByEntity(entityId, "TASK")).thenReturn(List.of(entry));

    List<ActivityEntry> result = service.listByEntity("TASK", entityId);

    assertThat(result).containsExactly(entry);
    verify(activityLogRepository).findByEntity(entityId, "TASK");
  }

  @Test
  void listByEntityRejectsBlankEntityType() {
    UUID entityId = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd");

    assertThatIllegalArgumentException()
        .isThrownBy(() -> service.listByEntity("  ", entityId))
        .withMessageContaining("entityType");
  }

  @Test
  void listByEntityRejectsNullEntityId() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> service.listByEntity("TASK", null))
        .withMessageContaining("entityId");
  }

  @Test
  void listRecentReturnsActivities() {
    ActivityEntry entry =
        new ActivityEntry(
            UUID.fromString("11111111-1111-1111-1111-111111111111"),
            "TASK",
            UUID.fromString("22222222-2222-2222-2222-222222222222"),
            UUID.fromString("33333333-3333-3333-3333-333333333333"),
            "CREATED",
            null,
            Instant.parse("2024-02-01T00:00:00Z"));
    when(activityLogRepository.findRecentByEntityType("TASK", 5)).thenReturn(List.of(entry));

    List<ActivityEntry> result = service.listRecent("TASK", 5);

    assertThat(result).containsExactly(entry);
    verify(activityLogRepository).findRecentByEntityType("TASK", 5);
  }

  @Test
  void listRecentRejectsBlankEntityType() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> service.listRecent("", 1))
        .withMessageContaining("entityType");
  }

  @Test
  void listRecentRejectsNonPositiveLimit() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> service.listRecent("TASK", 0))
        .withMessage("limit must be positive");
  }
}
