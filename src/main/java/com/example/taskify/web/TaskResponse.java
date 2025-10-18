package com.example.taskify.web;

import com.example.taskify.domain.task.TaskPriority;
import com.example.taskify.domain.task.TaskStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record TaskResponse(
    UUID id,
    UUID projectId,
    String title,
    String description,
    TaskPriority priority,
    TaskStatus status,
    UUID assigneeId,
    LocalDate dueDate,
    Instant completedAt,
    UUID createdBy,
    Instant createdAt,
    UUID updatedBy,
    Instant updatedAt) {}
