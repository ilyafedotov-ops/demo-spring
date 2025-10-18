package com.example.taskify.web;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ProjectOwnerChangeRequest(@NotNull UUID ownerId) {}
