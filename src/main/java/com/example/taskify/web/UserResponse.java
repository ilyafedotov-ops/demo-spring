package com.example.taskify.web;

import com.example.taskify.domain.user.UserRole;
import com.example.taskify.domain.user.UserStatus;
import java.time.Instant;
import java.util.UUID;

public record UserResponse(
    UUID id,
    String username,
    String email,
    String displayName,
    UserRole role,
    UserStatus status,
    Instant lastLoginAt,
    Instant createdAt,
    Instant updatedAt) {}
