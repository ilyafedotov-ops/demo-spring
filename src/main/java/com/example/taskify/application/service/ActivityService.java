package com.example.taskify.application.service;

import com.example.taskify.application.port.out.ActivityLogRepository;
import com.example.taskify.domain.activity.ActivityEntry;
import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

@Service
public class ActivityService {

  private final ActivityLogRepository activityLogRepository;
  private final Supplier<UUID> uuidSupplier;
  private final Clock clock;

  public ActivityService(
      ActivityLogRepository activityLogRepository, Supplier<UUID> uuidSupplier, Clock clock) {
    this.activityLogRepository = activityLogRepository;
    this.uuidSupplier = uuidSupplier;
    this.clock = clock;
  }

  @Transactional
  public ActivityEntry record(
      String entityType, UUID entityId, UUID actorId, String action, Map<String, Object> payload) {
    Assert.hasText(entityType, "entityType must not be blank");
    Assert.hasText(action, "action must not be blank");
    ActivityEntry entry =
        new ActivityEntry(
            uuidSupplier.get(), entityType, entityId, actorId, action, payload, clock.instant());
    return activityLogRepository.save(entry);
  }

  @Transactional(readOnly = true)
  public List<ActivityEntry> findForEntity(String entityType, UUID entityId) {
    return activityLogRepository.findByEntity(entityId, entityType);
  }
}
