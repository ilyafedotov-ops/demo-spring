package com.example.taskify.web;

import java.time.Instant;
import java.util.UUID;

public record CommentResponse(
    UUID id, UUID taskId, UUID authorId, String body, Instant createdAt) {}
