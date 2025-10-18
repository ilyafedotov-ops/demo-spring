package com.example.taskify.domain.user;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import org.springframework.util.Assert;

public class User {
  private static final Pattern EMAIL_PATTERN =
      Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", Pattern.CASE_INSENSITIVE);

  private final UUID id;
  private String username;
  private String email;
  private String displayName;
  private UserRole role;
  private UserStatus status;
  private Instant lastLoginAt;
  private final Instant createdAt;
  private Instant updatedAt;

  private User(
      UUID id,
      String username,
      String email,
      String displayName,
      UserRole role,
      UserStatus status,
      Instant lastLoginAt,
      Instant createdAt,
      Instant updatedAt) {
    this.id = id;
    this.username = username;
    this.email = email;
    this.displayName = displayName;
    this.role = role;
    this.status = status;
    this.lastLoginAt = lastLoginAt;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public static User register(
      UUID id, String username, String email, UserRole role, Supplier<Instant> timestampSupplier) {
    Objects.requireNonNull(id, "id must not be null");
    Assert.hasText(username, "username must not be blank");
    validateEmail(email);
    Objects.requireNonNull(role, "role must not be null");
    Instant now = Objects.requireNonNull(timestampSupplier.get(), "timestamp must not be null");
    return new User(
        id, username.strip(), email.toLowerCase(), null, role, UserStatus.INVITED, null, now, now);
  }

  public static User rehydrate(
      UUID id,
      String username,
      String email,
      String displayName,
      UserRole role,
      UserStatus status,
      Instant lastLoginAt,
      Instant createdAt,
      Instant updatedAt) {
    Objects.requireNonNull(id, "id must not be null");
    Objects.requireNonNull(username, "username must not be null");
    Objects.requireNonNull(email, "email must not be null");
    Objects.requireNonNull(role, "role must not be null");
    Objects.requireNonNull(status, "status must not be null");
    Objects.requireNonNull(createdAt, "createdAt must not be null");
    Objects.requireNonNull(updatedAt, "updatedAt must not be null");
    return new User(
        id, username, email, displayName, role, status, lastLoginAt, createdAt, updatedAt);
  }

  private static void validateEmail(String email) {
    Assert.hasText(email, "email must not be blank");
    if (!EMAIL_PATTERN.matcher(email).matches()) {
      throw new IllegalArgumentException("Invalid email address");
    }
  }

  public UUID getId() {
    return id;
  }

  public String getUsername() {
    return username;
  }

  public String getEmail() {
    return email;
  }

  public Optional<String> getDisplayName() {
    return Optional.ofNullable(displayName);
  }

  public UserRole getRole() {
    return role;
  }

  public UserStatus getStatus() {
    return status;
  }

  public Optional<Instant> getLastLoginAt() {
    return Optional.ofNullable(lastLoginAt);
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public void activate(Supplier<Instant> timestampSupplier) {
    if (status == UserStatus.ACTIVE) {
      return;
    }
    this.status = UserStatus.ACTIVE;
    this.updatedAt = Objects.requireNonNull(timestampSupplier.get(), "timestamp must not be null");
  }

  public void lock(Supplier<Instant> timestampSupplier) {
    this.status = UserStatus.LOCKED;
    this.updatedAt = Objects.requireNonNull(timestampSupplier.get(), "timestamp must not be null");
  }

  public void disable(Supplier<Instant> timestampSupplier) {
    this.status = UserStatus.DISABLED;
    this.updatedAt = Objects.requireNonNull(timestampSupplier.get(), "timestamp must not be null");
  }

  public void changeRole(UserRole newRole, Supplier<Instant> timestampSupplier) {
    Objects.requireNonNull(newRole, "newRole must not be null");
    if (this.role != newRole) {
      this.role = newRole;
      this.updatedAt =
          Objects.requireNonNull(timestampSupplier.get(), "timestamp must not be null");
    }
  }

  public void updateProfile(
      String newUsername, String newDisplayName, Supplier<Instant> timestamp) {
    Assert.hasText(newUsername, "newUsername must not be blank");
    String trimmed = newUsername.strip();
    if (!trimmed.equals(this.username) || !Objects.equals(newDisplayName, this.displayName)) {
      this.username = trimmed;
      this.displayName = newDisplayName;
      this.updatedAt = Objects.requireNonNull(timestamp.get(), "timestamp must not be null");
    }
  }

  public void recordSuccessfulLogin(Supplier<Instant> timestampSupplier) {
    Instant now = Objects.requireNonNull(timestampSupplier.get(), "timestamp must not be null");
    this.lastLoginAt = now;
    this.updatedAt = now;
  }
}
