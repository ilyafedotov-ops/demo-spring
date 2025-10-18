package com.example.taskify.infrastructure.persistence.jpa.adapter;

import com.example.taskify.application.port.out.UserDirectory;
import com.example.taskify.infrastructure.persistence.jpa.repository.UserJpaRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class DatabaseUserDirectory implements UserDirectory {

  private final UserJpaRepository userJpaRepository;

  public DatabaseUserDirectory(UserJpaRepository userJpaRepository) {
    this.userJpaRepository = userJpaRepository;
  }

  @Override
  public boolean existsById(UUID userId) {
    return userJpaRepository.existsById(userId);
  }

  @Override
  public Optional<UserSummary> findSummary(UUID userId) {
    return userJpaRepository
        .findById(userId)
        .map(entity -> new UserSummary(entity.getId(), entity.getUsername()));
  }
}
