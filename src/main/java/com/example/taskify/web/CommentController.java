package com.example.taskify.web;

import com.example.taskify.application.service.CommentService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks/{taskId}/comments")
@Validated
public class CommentController {

  private final CommentService commentService;
  private final CommentMapper commentMapper;
  private final ActorResolver actorResolver;

  public CommentController(
      CommentService commentService, CommentMapper commentMapper, ActorResolver actorResolver) {
    this.commentService = commentService;
    this.commentMapper = commentMapper;
    this.actorResolver = actorResolver;
  }

  @PostMapping
  public ResponseEntity<CommentResponse> addComment(
      @PathVariable UUID taskId,
      @Valid @RequestBody CommentCreateRequest request,
      Authentication authentication) {
    UUID actorId = actorResolver.requireActor(authentication);
    var created = commentService.addComment(taskId, actorId, request.body());
    CommentResponse response = commentMapper.toResponse(created);
    URI location = URI.create("/api/tasks/" + taskId + "/comments/" + response.id());
    return ResponseEntity.created(location).body(response);
  }

  @GetMapping
  public List<CommentResponse> listComments(@PathVariable UUID taskId) {
    return commentMapper.toResponses(commentService.listComments(taskId));
  }

  @DeleteMapping("/{commentId}")
  public ResponseEntity<Void> deleteComment(
      @PathVariable UUID taskId, @PathVariable UUID commentId, Authentication authentication) {
    UUID actorId = actorResolver.requireActor(authentication);
    boolean removed = commentService.deleteComment(commentId, actorId);
    if (!removed) {
      throw new IllegalArgumentException("Comment not found");
    }
    return ResponseEntity.noContent().build();
  }
}
