package com.example.taskify.application.service;

import com.example.taskify.application.exception.ResourceNotFoundException;
import com.example.taskify.application.port.out.CommentRepository;
import com.example.taskify.application.port.out.TaskRepository;
import com.example.taskify.application.port.out.UserDirectory;
import com.example.taskify.domain.comment.Comment;
import java.time.Clock;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

@Service
public class CommentService {

  private final CommentRepository commentRepository;
  private final TaskRepository taskRepository;
  private final UserDirectory userDirectory;
  private final ActivityService activityService;
  private final Supplier<UUID> uuidSupplier;
  private final Clock clock;

  public CommentService(
      CommentRepository commentRepository,
      TaskRepository taskRepository,
      UserDirectory userDirectory,
      ActivityService activityService,
      Supplier<UUID> uuidSupplier,
      Clock clock) {
    this.commentRepository = commentRepository;
    this.taskRepository = taskRepository;
    this.userDirectory = userDirectory;
    this.activityService = activityService;
    this.uuidSupplier = uuidSupplier;
    this.clock = clock;
  }

  @Transactional
  public Comment addComment(UUID taskId, UUID authorId, String body) {
    Assert.notNull(taskId, "taskId must not be null");
    Assert.notNull(authorId, "authorId must not be null");
    Assert.hasText(body, "body must not be blank");
    taskRepository
        .findById(taskId)
        .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
    if (!userDirectory.existsById(authorId)) {
      throw new ResourceNotFoundException("Author does not exist");
    }
    Comment comment =
        new Comment(uuidSupplier.get(), taskId, authorId, body.strip(), clock.instant());
    Comment persisted = commentRepository.save(comment);
    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("taskId", taskId);
    payload.put("body", persisted.body());
    activityService.record(
        ActivityCodebook.ENTITY_COMMENT,
        persisted.id(),
        authorId,
        ActivityCodebook.ACTION_COMMENT_ADDED,
        payload);
    return persisted;
  }

  @Transactional(readOnly = true)
  public List<Comment> listComments(UUID taskId) {
    return commentRepository.findByTaskId(taskId);
  }

  @Transactional
  public boolean deleteComment(UUID taskId, UUID commentId, UUID actorId) {
    Assert.notNull(taskId, "taskId must not be null");
    Assert.notNull(commentId, "commentId must not be null");
    Assert.notNull(actorId, "actorId must not be null");
    return commentRepository
        .findById(commentId)
        .filter(existing -> existing.taskId().equals(taskId))
        .map(
            existing -> {
              commentRepository.delete(commentId);
              Map<String, Object> payload = new LinkedHashMap<>();
              payload.put("taskId", existing.taskId());
              activityService.record(
                  ActivityCodebook.ENTITY_COMMENT,
                  commentId,
                  actorId,
                  ActivityCodebook.ACTION_COMMENT_REMOVED,
                  payload);
              return true;
            })
        .orElse(false);
  }
}
