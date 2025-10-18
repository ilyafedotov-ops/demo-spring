package com.example.taskify.application.port.out;

import com.example.taskify.domain.activity.ActivityEntry;
import java.util.List;
import java.util.UUID;

public interface ActivityLogRepository {
  ActivityEntry save(ActivityEntry entry);

  List<ActivityEntry> findByEntity(UUID entityId, String entityType);

  List<ActivityEntry> findRecentByEntityType(String entityType, int limit);
}
