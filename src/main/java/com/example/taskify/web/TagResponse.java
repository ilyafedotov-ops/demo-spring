package com.example.taskify.web;

import java.time.Instant;
import java.util.UUID;

public record TagResponse(
    UUID id, String name, String color, Instant createdAt, Instant updatedAt) {}
