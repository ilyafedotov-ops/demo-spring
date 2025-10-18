package com.example.taskify.application.port.out;

import java.util.Optional;
import java.util.UUID;

public interface UserDirectory {
  boolean existsById(UUID userId);

  Optional<UserSummary> findSummary(UUID userId);

  record UserSummary(UUID id, String username) {}
}
