package com.example.taskify.config.audit;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Resolves the current auditor from Spring Security. When no authenticated principal is available,
 * auditing defaults to empty.
 */
public class SecurityAuditorAware implements AuditorAware<UUID> {

  @Override
  public Optional<UUID> getCurrentAuditor() {
    return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
        .filter(Authentication::isAuthenticated)
        .map(Authentication::getName)
        .flatMap(this::parseUuid);
  }

  private Optional<UUID> parseUuid(String candidate) {
    try {
      return Optional.of(UUID.fromString(candidate));
    } catch (IllegalArgumentException ex) {
      return Optional.empty();
    }
  }
}
