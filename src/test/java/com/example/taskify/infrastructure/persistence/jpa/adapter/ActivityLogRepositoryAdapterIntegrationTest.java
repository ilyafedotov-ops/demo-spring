package com.example.taskify.infrastructure.persistence.jpa.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.taskify.application.port.out.ActivityLogRepository;
import com.example.taskify.domain.activity.ActivityEntry;
import com.example.taskify.infrastructure.persistence.jpa.mapper.ActivityLogEntityMapper;
import com.example.taskify.infrastructure.persistence.jpa.repository.ActivityLogJpaRepository;
import com.example.taskify.support.PostgresIntegrationTest;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
  ActivityLogRepositoryAdapter.class,
  ActivityLogEntityMapper.class,
  com.example.taskify.config.TaskifyPropertiesConfiguration.class,
  com.example.taskify.config.PersistenceConfig.class
})
class ActivityLogRepositoryAdapterIntegrationTest extends PostgresIntegrationTest {

  @Autowired private ActivityLogRepository activityLogRepository;
  @Autowired private ActivityLogJpaRepository activityLogJpaRepository;

  @Test
  void persistsAndFetchesEntries() {
    UUID entityId = UUID.randomUUID();
    Instant now = Instant.now();

    ActivityEntry entry1 =
        new ActivityEntry(
            UUID.randomUUID(),
            "TASK",
            entityId,
            null,
            "STATUS_CHANGED",
            Map.of("from", "TODO", "to", "IN_PROGRESS"),
            now);
    ActivityEntry entry2 =
        new ActivityEntry(
            UUID.randomUUID(),
            "TASK",
            entityId,
            null,
            "ASSIGNED",
            Map.of("assignee", "john"),
            now.plusSeconds(10));

    activityLogRepository.save(entry1);
    activityLogRepository.save(entry2);

    assertThat(activityLogJpaRepository.count()).isEqualTo(2);

    List<ActivityEntry> entries = activityLogRepository.findByEntity(entityId, "TASK");
    assertThat(entries)
        .extracting(ActivityEntry::action)
        .containsExactly("STATUS_CHANGED", "ASSIGNED");
    assertThat(entries.getFirst().payload()).containsEntry("from", "TODO");
  }

  @Test
  void findRecentByEntityTypeReturnsLatestFirst() {
    UUID entityId = UUID.randomUUID();
    activityLogRepository.save(
        new ActivityEntry(
            UUID.randomUUID(),
            "TASK",
            entityId,
            null,
            "FIRST",
            Map.of(),
            Instant.parse("2025-01-01T00:00:00Z")));
    activityLogRepository.save(
        new ActivityEntry(
            UUID.randomUUID(),
            "TASK",
            entityId,
            null,
            "SECOND",
            Map.of(),
            Instant.parse("2025-01-02T00:00:00Z")));

    List<ActivityEntry> entries = activityLogRepository.findRecentByEntityType("TASK", 1);

    assertThat(entries).hasSize(1);
    assertThat(entries.getFirst().action()).isEqualTo("SECOND");
  }
}
