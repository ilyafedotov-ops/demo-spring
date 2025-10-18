package com.example.taskify.web;

import com.example.taskify.domain.task.Task;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TaskMapper {

  @org.mapstruct.Mapping(
      target = "assigneeId",
      expression = "java(task.getAssigneeId().orElse(null))")
  @org.mapstruct.Mapping(target = "dueDate", expression = "java(task.getDueDate().orElse(null))")
  @org.mapstruct.Mapping(
      target = "completedAt",
      expression = "java(task.getCompletedAt().orElse(null))")
  @org.mapstruct.Mapping(
      target = "updatedBy",
      expression = "java(task.getUpdatedBy().orElse(null))")
  TaskResponse toResponse(Task task);

  List<TaskResponse> toResponses(List<Task> tasks);
}
