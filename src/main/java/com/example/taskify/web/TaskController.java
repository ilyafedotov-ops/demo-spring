package com.example.taskify.web;

import com.example.taskify.application.service.TaskService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks")
@Validated
public class TaskController {

  private final TaskService taskService;
  private final TaskMapper taskMapper;
  private final ActorResolver actorResolver;

  public TaskController(
      TaskService taskService, TaskMapper taskMapper, ActorResolver actorResolver) {
    this.taskService = taskService;
    this.taskMapper = taskMapper;
    this.actorResolver = actorResolver;
  }

  @PostMapping
  public ResponseEntity<TaskResponse> createTask(
      @Valid @RequestBody TaskCreateRequest request, Authentication authentication) {
    UUID actorId = actorResolver.requireActor(authentication);
    var created =
        taskService.createTask(
            request.projectId(),
            request.title(),
            request.description(),
            request.priority(),
            request.status(),
            request.assigneeId(),
            request.dueDate(),
            actorId);
    TaskResponse response = taskMapper.toResponse(created);
    URI location = URI.create("/api/tasks/" + response.id());
    return ResponseEntity.created(location).body(response);
  }

  @GetMapping("/{taskId}")
  public TaskResponse getTask(@PathVariable UUID taskId) {
    return taskMapper.toResponse(taskService.getTask(taskId));
  }

  @GetMapping
  public List<TaskResponse> listTasks(@RequestParam("projectId") UUID projectId) {
    return taskMapper.toResponses(taskService.listTasks(projectId));
  }

  @PutMapping("/{taskId}")
  public TaskResponse updateDetails(
      @PathVariable UUID taskId,
      @Valid @RequestBody TaskUpdateDetailsRequest request,
      Authentication authentication) {
    UUID actorId = actorResolver.requireActor(authentication);
    return taskMapper.toResponse(
        taskService.updateDetails(
            taskId,
            request.title(),
            request.description(),
            request.priority(),
            request.dueDate(),
            actorId));
  }

  @PatchMapping("/{taskId}/assignee")
  public TaskResponse assignTask(
      @PathVariable UUID taskId,
      @RequestBody TaskAssignRequest request,
      Authentication authentication) {
    UUID actorId = actorResolver.requireActor(authentication);
    return taskMapper.toResponse(taskService.assignTask(taskId, request.assigneeId(), actorId));
  }

  @PatchMapping("/{taskId}/status")
  public TaskResponse changeStatus(
      @PathVariable UUID taskId,
      @Valid @RequestBody TaskStatusUpdateRequest request,
      Authentication authentication) {
    UUID actorId = actorResolver.requireActor(authentication);
    return taskMapper.toResponse(taskService.changeStatus(taskId, request.status(), actorId));
  }

  @PostMapping("/{taskId}/tags/{tagId}")
  public ResponseEntity<Void> attachTag(
      @PathVariable UUID taskId, @PathVariable UUID tagId, Authentication authentication) {
    UUID actorId = actorResolver.requireActor(authentication);
    taskService.attachTag(taskId, tagId, actorId);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{taskId}/tags/{tagId}")
  public ResponseEntity<Void> detachTag(
      @PathVariable UUID taskId, @PathVariable UUID tagId, Authentication authentication) {
    UUID actorId = actorResolver.requireActor(authentication);
    taskService.detachTag(taskId, tagId, actorId);
    return ResponseEntity.noContent().build();
  }
}
