package com.example.taskify.application.service;

public final class ActivityCodebook {

  private ActivityCodebook() {}

  public static final String ENTITY_TASK = "TASK";
  public static final String ENTITY_COMMENT = "COMMENT";

  public static final String ACTION_TASK_CREATED = "TASK_CREATED";
  public static final String ACTION_TASK_STATUS_CHANGED = "TASK_STATUS_CHANGED";
  public static final String ACTION_TASK_ASSIGNED = "TASK_ASSIGNED";
  public static final String ACTION_TASK_UPDATED = "TASK_UPDATED";
  public static final String ACTION_TASK_TAGGED = "TASK_TAGGED";
  public static final String ACTION_TASK_UNTAGGED = "TASK_UNTAGGED";
  public static final String ACTION_COMMENT_ADDED = "COMMENT_ADDED";
  public static final String ACTION_COMMENT_REMOVED = "COMMENT_REMOVED";
}
