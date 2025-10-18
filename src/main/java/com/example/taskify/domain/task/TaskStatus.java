package com.example.taskify.domain.task;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public enum TaskStatus {
  TODO,
  IN_PROGRESS,
  BLOCKED,
  DONE,
  CANCELLED;

  private static final Map<TaskStatus, Set<TaskStatus>> ALLOWED_TRANSITIONS;

  static {
    Map<TaskStatus, Set<TaskStatus>> transitions = new EnumMap<>(TaskStatus.class);
    transitions.put(TODO, EnumSet.of(IN_PROGRESS, BLOCKED, CANCELLED));
    transitions.put(IN_PROGRESS, EnumSet.of(BLOCKED, DONE, CANCELLED));
    transitions.put(BLOCKED, EnumSet.of(IN_PROGRESS, CANCELLED));
    transitions.put(DONE, EnumSet.of(CANCELLED));
    transitions.put(CANCELLED, EnumSet.noneOf(TaskStatus.class));
    ALLOWED_TRANSITIONS = Map.copyOf(transitions);
  }

  public boolean canTransitionTo(TaskStatus next) {
    return ALLOWED_TRANSITIONS.getOrDefault(this, EnumSet.noneOf(TaskStatus.class)).contains(next);
  }
}
