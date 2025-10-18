package com.example.taskify.web;

import com.example.taskify.domain.task.TaskPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record TaskUpdateDetailsRequest(
    @NotBlank String title,
    String description,
    @NotNull TaskPriority priority,
    LocalDate dueDate) {}
