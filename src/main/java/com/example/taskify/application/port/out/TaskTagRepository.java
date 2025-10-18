package com.example.taskify.application.port.out;

import java.util.Set;
import java.util.UUID;

public interface TaskTagRepository {

  boolean isTagAttached(UUID taskId, UUID tagId);

  void attach(UUID taskId, UUID tagId);

  boolean detach(UUID taskId, UUID tagId);

  Set<UUID> listTagIds(UUID taskId);
}
