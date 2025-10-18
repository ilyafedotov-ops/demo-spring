package com.example.taskify.web;

import com.example.taskify.domain.project.ProjectStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record ProjectCreateRequest(
    @NotBlank String name,
    String description,
    @NotNull UUID ownerId,
    ProjectStatus status,
    LocalDate startDate,
    LocalDate endDate) {}
