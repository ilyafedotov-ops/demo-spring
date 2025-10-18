package com.example.taskify.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.taskify.application.port.out.ActivityLogRepository;
import com.example.taskify.domain.activity.ActivityEntry;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ActivityServiceTest {

  @Mock private ActivityLogRepository activityLogRepository;

  private ActivityService activityService;
  private Supplier<UUID> uuidSupplier;
  private static final Instant NOW = Instant.parse("2025-01-02T00:00:00Z");

  @BeforeEach
  void setUp() {
    uuidSupplier = () -> UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
    activityService =
        new ActivityService(activityLogRepository, uuidSupplier, Clock.fixed(NOW, ZoneOffset.UTC));
  }

  @Test
  void recordBuildsEntryAndDelegatesToRepository() {
    ArgumentCaptor<ActivityEntry> captor = ArgumentCaptor.forClass(ActivityEntry.class);
    when(activityLogRepository.save(captor.capture()))
        .thenAnswer(invocation -> invocation.getArgument(0));

    ActivityEntry entry =
        activityService.record(
            "TASK",
            UUID.fromString("11111111-2222-3333-4444-555555555555"),
            UUID.fromString("99999999-8888-7777-6666-555555555555"),
            "STATUS_CHANGED",
            Map.of("from", "TODO"));

    assertThat(entry.id()).isEqualTo(uuidSupplier.get());
    assertThat(entry.payload()).containsEntry("from", "TODO");
    assertThat(entry.occurredAt()).isEqualTo(NOW);
    verify(activityLogRepository).save(captor.getValue());
  }

  @Test
  void findForEntityDelegatesToRepository() {
    UUID entityId = UUID.randomUUID();
    when(activityLogRepository.findByEntity(entityId, "TASK")).thenReturn(List.of());

    assertThat(activityService.findForEntity("TASK", entityId)).isEmpty();
    verify(activityLogRepository).findByEntity(entityId, "TASK");
  }
}
