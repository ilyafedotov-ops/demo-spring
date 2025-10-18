package com.example.taskify.web;

import com.example.taskify.application.service.ProjectService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects")
@Validated
public class ProjectController {

  private final ProjectService projectService;
  private final ProjectMapper projectMapper;
  private final ActorResolver actorResolver;

  public ProjectController(
      ProjectService projectService, ProjectMapper projectMapper, ActorResolver actorResolver) {
    this.projectService = projectService;
    this.projectMapper = projectMapper;
    this.actorResolver = actorResolver;
  }

  @PostMapping
  public ResponseEntity<ProjectResponse> createProject(
      @Valid @RequestBody ProjectCreateRequest request, Authentication authentication) {
    UUID actorId = actorResolver.requireActor(authentication);
    var created =
        projectService.createProject(
            request.name(),
            request.description(),
            request.ownerId(),
            request.status(),
            request.startDate(),
            request.endDate(),
            actorId);
    ProjectResponse response = projectMapper.toResponse(created);
    URI location = URI.create("/api/projects/" + response.id());
    return ResponseEntity.created(location).body(response);
  }

  @GetMapping("/{projectId}")
  public ProjectResponse getProject(@PathVariable UUID projectId) {
    return projectMapper.toResponse(projectService.getProject(projectId));
  }

  @GetMapping
  public List<ProjectResponse> listProjects() {
    return projectMapper.toResponses(projectService.listProjects());
  }

  @PutMapping("/{projectId}")
  public ProjectResponse updateProject(
      @PathVariable UUID projectId,
      @Valid @RequestBody ProjectUpdateRequest request,
      Authentication authentication) {
    UUID actorId = actorResolver.requireActor(authentication);
    return projectMapper.toResponse(
        projectService.updateDetails(
            projectId,
            request.name(),
            request.description(),
            actorId,
            request.startDate(),
            request.endDate()));
  }

  @PatchMapping("/{projectId}/status")
  public ProjectResponse changeStatus(
      @PathVariable UUID projectId,
      @Valid @RequestBody ProjectStatusUpdateRequest request,
      Authentication authentication) {
    UUID actorId = actorResolver.requireActor(authentication);
    return projectMapper.toResponse(
        projectService.changeStatus(projectId, request.status(), actorId));
  }

  @PatchMapping("/{projectId}/owner")
  public ProjectResponse changeOwner(
      @PathVariable UUID projectId,
      @Valid @RequestBody ProjectOwnerChangeRequest request,
      Authentication authentication) {
    UUID actorId = actorResolver.requireActor(authentication);
    return projectMapper.toResponse(
        projectService.reassignOwner(projectId, request.ownerId(), actorId));
  }
}
