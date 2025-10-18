package com.example.taskify.web;

import jakarta.validation.constraints.NotBlank;

public record CommentCreateRequest(@NotBlank String body) {}
