package com.example.taskify.infrastructure.persistence.jpa.mapper;

import com.example.taskify.domain.comment.Comment;
import com.example.taskify.infrastructure.persistence.jpa.entity.CommentJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class CommentEntityMapper {

  public CommentJpaEntity toEntity(Comment comment) {
    CommentJpaEntity entity = new CommentJpaEntity();
    entity.setId(comment.id());
    entity.setTaskId(comment.taskId());
    entity.setAuthorId(comment.authorId());
    entity.setBody(comment.body());
    entity.setCreatedAt(comment.createdAt());
    return entity;
  }

  public Comment toDomain(CommentJpaEntity entity) {
    return new Comment(
        entity.getId(),
        entity.getTaskId(),
        entity.getAuthorId(),
        entity.getBody(),
        entity.getCreatedAt());
  }
}
