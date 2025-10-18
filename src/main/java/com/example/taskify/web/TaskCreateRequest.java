package com.example.taskify.web;

import com.example.taskify.domain.task.TaskPriority;
import com.example.taskify.domain.task.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record TaskCreateRequest(
    @NotNull UUID projectId,
    @NotBlank String title,
    String description,
    TaskPriority priority,
    TaskStatus status,
    UUID assigneeId,
    LocalDate dueDate) {}
