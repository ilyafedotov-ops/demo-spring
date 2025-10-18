package com.example.taskify.web;

import com.example.taskify.domain.project.ProjectStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ProjectResponse(
    UUID id,
    String name,
    String description,
    UUID ownerId,
    ProjectStatus status,
    LocalDate startDate,
    LocalDate endDate,
    UUID createdBy,
    Instant createdAt,
    UUID updatedBy,
    Instant updatedAt) {}
