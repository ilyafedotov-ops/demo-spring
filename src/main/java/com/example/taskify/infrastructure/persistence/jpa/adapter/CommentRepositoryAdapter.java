package com.example.taskify.infrastructure.persistence.jpa.adapter;

import com.example.taskify.application.port.out.CommentRepository;
import com.example.taskify.domain.comment.Comment;
import com.example.taskify.infrastructure.persistence.jpa.entity.CommentJpaEntity;
import com.example.taskify.infrastructure.persistence.jpa.mapper.CommentEntityMapper;
import com.example.taskify.infrastructure.persistence.jpa.repository.CommentJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class CommentRepositoryAdapter implements CommentRepository {

  private final CommentJpaRepository jpaRepository;
  private final CommentEntityMapper mapper;

  public CommentRepositoryAdapter(CommentJpaRepository jpaRepository, CommentEntityMapper mapper) {
    this.jpaRepository = jpaRepository;
    this.mapper = mapper;
  }

  @Override
  public Comment save(Comment comment) {
    CommentJpaEntity saved = jpaRepository.save(mapper.toEntity(comment));
    return mapper.toDomain(saved);
  }

  @Override
  public List<Comment> findByTaskId(UUID taskId) {
    return jpaRepository.findByTaskIdOrderByCreatedAtAsc(taskId).stream()
        .map(mapper::toDomain)
        .toList();
  }

  @Override
  public Optional<Comment> findById(UUID commentId) {
    return jpaRepository.findById(commentId).map(mapper::toDomain);
  }

  @Override
  public void delete(UUID commentId) {
    jpaRepository.deleteById(commentId);
  }

  @Override
  public Optional<Comment> deleteReturning(UUID commentId) {
    return jpaRepository
        .findById(commentId)
        .map(
            entity -> {
              jpaRepository.delete(entity);
              return mapper.toDomain(entity);
            });
  }
}
