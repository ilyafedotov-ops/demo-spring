package com.example.taskify.web;

import jakarta.validation.constraints.NotBlank;

public record TagCreateRequest(@NotBlank String name, @NotBlank String color) {}
