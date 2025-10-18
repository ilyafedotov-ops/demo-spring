package com.example.taskify.domain.tag;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import org.springframework.util.Assert;

public class Tag {
  private final UUID id;
  private String name;
  private String color;
  private final Instant createdAt;
  private Instant updatedAt;

  private Tag(UUID id, String name, String color, Instant createdAt, Instant updatedAt) {
    this.id = id;
    this.name = name;
    this.color = color;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public static Tag create(UUID id, String name, String color, Instant timestamp) {
    Objects.requireNonNull(id, "id must not be null");
    Assert.hasText(name, "name must not be blank");
    Assert.hasText(color, "color must not be blank");
    Objects.requireNonNull(timestamp, "timestamp must not be null");
    return new Tag(id, name.strip(), color, timestamp, timestamp);
  }

  public static Tag rehydrate(
      UUID id, String name, String color, Instant createdAt, Instant updatedAt) {
    Objects.requireNonNull(id, "id must not be null");
    Objects.requireNonNull(name, "name must not be null");
    Objects.requireNonNull(color, "color must not be null");
    Objects.requireNonNull(createdAt, "createdAt must not be null");
    Objects.requireNonNull(updatedAt, "updatedAt must not be null");
    return new Tag(id, name, color, createdAt, updatedAt);
  }

  public UUID getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getColor() {
    return color;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public void rename(String newName, Instant timestamp) {
    Assert.hasText(newName, "newName must not be blank");
    this.name = newName.strip();
    this.updatedAt = Objects.requireNonNull(timestamp, "timestamp must not be null");
  }

  public void recolor(String newColor, Instant timestamp) {
    Assert.hasText(newColor, "newColor must not be blank");
    this.color = newColor;
    this.updatedAt = Objects.requireNonNull(timestamp, "timestamp must not be null");
  }
}
