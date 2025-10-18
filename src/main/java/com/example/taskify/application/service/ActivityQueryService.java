package com.example.taskify.application.service;

import com.example.taskify.application.port.out.ActivityLogRepository;
import com.example.taskify.domain.activity.ActivityEntry;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

@Service
public class ActivityQueryService {

  private final ActivityLogRepository activityLogRepository;

  public ActivityQueryService(ActivityLogRepository activityLogRepository) {
    this.activityLogRepository = activityLogRepository;
  }

  @Transactional(readOnly = true)
  public List<ActivityEntry> listByEntity(String entityType, UUID entityId) {
    Assert.hasText(entityType, "entityType must not be blank");
    Assert.notNull(entityId, "entityId must not be null");
    return activityLogRepository.findByEntity(entityId, entityType);
  }

  @Transactional(readOnly = true)
  public List<ActivityEntry> listRecent(String entityType, int limit) {
    Assert.hasText(entityType, "entityType must not be blank");
    if (limit <= 0) {
      throw new IllegalArgumentException("limit must be positive");
    }
    return activityLogRepository.findRecentByEntityType(entityType, limit);
  }
}
