package com.example.taskify.infrastructure.persistence.jpa.mapper;

import com.example.taskify.domain.user.User;
import com.example.taskify.infrastructure.persistence.jpa.entity.UserJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class UserEntityMapper {

  public User toDomain(UserJpaEntity entity) {
    return User.rehydrate(
        entity.getId(),
        entity.getUsername(),
        entity.getEmail(),
        entity.getDisplayName(),
        entity.getRole(),
        entity.getStatus(),
        entity.getLastLoginAt(),
        entity.getCreatedAt(),
        entity.getUpdatedAt());
  }
}
