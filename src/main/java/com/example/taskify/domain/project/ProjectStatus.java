package com.example.taskify.domain.project;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public enum ProjectStatus {
  PLANNED,
  ACTIVE,
  ON_HOLD,
  COMPLETED,
  CANCELLED;

  private static final Map<ProjectStatus, Set<ProjectStatus>> ALLOWED_TRANSITIONS;

  static {
    Map<ProjectStatus, Set<ProjectStatus>> transitions = new EnumMap<>(ProjectStatus.class);
    transitions.put(PLANNED, EnumSet.of(ACTIVE, CANCELLED));
    transitions.put(ACTIVE, EnumSet.of(ON_HOLD, COMPLETED, CANCELLED));
    transitions.put(ON_HOLD, EnumSet.of(ACTIVE, CANCELLED));
    transitions.put(COMPLETED, EnumSet.noneOf(ProjectStatus.class));
    transitions.put(CANCELLED, EnumSet.noneOf(ProjectStatus.class));
    ALLOWED_TRANSITIONS = Map.copyOf(transitions);
  }

  public boolean canTransitionTo(ProjectStatus next) {
    return ALLOWED_TRANSITIONS
        .getOrDefault(this, EnumSet.noneOf(ProjectStatus.class))
        .contains(next);
  }
}
