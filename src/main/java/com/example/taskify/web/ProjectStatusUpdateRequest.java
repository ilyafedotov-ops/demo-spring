package com.example.taskify.web;

import com.example.taskify.domain.project.ProjectStatus;
import jakarta.validation.constraints.NotNull;

public record ProjectStatusUpdateRequest(@NotNull ProjectStatus status) {}
