package com.example.taskify.infrastructure.persistence.jpa.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.taskify.application.port.out.CommentRepository;
import com.example.taskify.domain.comment.Comment;
import com.example.taskify.domain.project.Project;
import com.example.taskify.domain.project.ProjectStatus;
import com.example.taskify.domain.task.Task;
import com.example.taskify.domain.task.TaskPriority;
import com.example.taskify.domain.task.TaskStatus;
import com.example.taskify.domain.user.UserRole;
import com.example.taskify.domain.user.UserStatus;
import com.example.taskify.infrastructure.persistence.jpa.entity.UserJpaEntity;
import com.example.taskify.infrastructure.persistence.jpa.mapper.CommentEntityMapper;
import com.example.taskify.infrastructure.persistence.jpa.mapper.ProjectEntityMapper;
import com.example.taskify.infrastructure.persistence.jpa.mapper.TaskEntityMapper;
import com.example.taskify.infrastructure.persistence.jpa.repository.CommentJpaRepository;
import com.example.taskify.infrastructure.persistence.jpa.repository.TaskJpaRepository;
import com.example.taskify.infrastructure.persistence.jpa.repository.UserJpaRepository;
import com.example.taskify.support.PostgresIntegrationTest;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
  CommentRepositoryAdapter.class,
  CommentEntityMapper.class,
  TaskRepositoryAdapter.class,
  TaskEntityMapper.class,
  ProjectRepositoryAdapter.class,
  ProjectEntityMapper.class,
  com.example.taskify.config.TaskifyPropertiesConfiguration.class,
  com.example.taskify.config.PersistenceConfig.class
})
class CommentRepositoryAdapterIntegrationTest extends PostgresIntegrationTest {

  @Autowired private CommentRepository commentRepository;
  @Autowired private CommentJpaRepository commentJpaRepository;
  @Autowired private TaskRepositoryAdapter taskRepository;
  @Autowired private ProjectRepositoryAdapter projectRepository;
  @Autowired private TaskJpaRepository taskJpaRepository;
  @Autowired private UserJpaRepository userJpaRepository;

  private UUID ownerId;
  private UUID taskId;

  @BeforeEach
  void setUp() {
    Instant now = Instant.now();
    ownerId = UUID.randomUUID();

    UserJpaEntity user = new UserJpaEntity();
    user.setId(ownerId);
    user.setUsername("commenter");
    user.setEmail("commenter@example.com");
    user.setPasswordHash("hashed");
    user.setDisplayName("Commenter");
    user.setRole(UserRole.TEAM_MEMBER);
    user.setStatus(UserStatus.ACTIVE);
    user.setLastLoginAt(now);
    user.setCreatedAt(now);
    user.setCreatedBy(ownerId);
    user.setUpdatedAt(now);
    user.setUpdatedBy(ownerId);
    userJpaRepository.save(user);

    UUID projectId = UUID.randomUUID();
    Project project =
        Project.create(
            projectId,
            "Collaboration",
            "Comment test project",
            ownerId,
            ProjectStatus.ACTIVE,
            LocalDate.now(),
            LocalDate.now().plusMonths(3),
            ownerId,
            Instant::now);
    projectRepository.save(project);

    taskId = UUID.randomUUID();
    Task task =
        Task.create(
            taskId,
            projectId,
            "Discuss requirements",
            null,
            TaskPriority.MEDIUM,
            TaskStatus.TODO,
            ownerId,
            LocalDate.now().plusDays(3),
            ownerId,
            Instant::now);
    taskRepository.save(task);
    assertThat(taskJpaRepository.existsById(taskId)).isTrue();
  }

  @Test
  void createAndRetrieveCommentsOrderedByCreatedAt() {
    UUID comment1Id = UUID.randomUUID();
    UUID comment2Id = UUID.randomUUID();

    commentRepository.save(new Comment(comment1Id, taskId, ownerId, "First note", Instant.now()));
    commentRepository.save(
        new Comment(comment2Id, taskId, ownerId, "Second note", Instant.now().plusSeconds(30)));

    List<Comment> comments = commentRepository.findByTaskId(taskId);
    assertThat(comments).extracting(Comment::id).containsExactly(comment1Id, comment2Id);

    assertThat(commentRepository.deleteReturning(comment1Id)).isPresent();
    assertThat(commentJpaRepository.existsById(comment1Id)).isFalse();
  }
}
