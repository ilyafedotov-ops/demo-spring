package com.example.taskify.domain.comment;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import org.springframework.util.Assert;

public record Comment(UUID id, UUID taskId, UUID authorId, String body, Instant createdAt) {
  public Comment {
    Objects.requireNonNull(id, "id must not be null");
    Objects.requireNonNull(taskId, "taskId must not be null");
    Objects.requireNonNull(authorId, "authorId must not be null");
    Assert.hasText(body, "body must not be blank");
    Objects.requireNonNull(createdAt, "createdAt must not be null");
  }
}
