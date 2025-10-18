package com.example.taskify.application.port.out;

import com.example.taskify.domain.comment.Comment;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CommentRepository {
  Comment save(Comment comment);

  List<Comment> findByTaskId(UUID taskId);

  Optional<Comment> findById(UUID commentId);

  void delete(UUID commentId);

  Optional<Comment> deleteReturning(UUID commentId);
}
