package com.example.taskify.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.taskify.application.exception.ResourceNotFoundException;
import com.example.taskify.application.port.out.CommentRepository;
import com.example.taskify.application.port.out.TaskRepository;
import com.example.taskify.application.port.out.UserDirectory;
import com.example.taskify.domain.comment.Comment;
import com.example.taskify.domain.task.Task;
import com.example.taskify.domain.task.TaskPriority;
import com.example.taskify.domain.task.TaskStatus;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

  @Mock private CommentRepository commentRepository;
  @Mock private TaskRepository taskRepository;
  @Mock private UserDirectory userDirectory;
  @Mock private ActivityService activityService;

  private CommentService commentService;
  private Supplier<UUID> uuidSupplier;
  private static final Instant NOW = Instant.parse("2025-01-01T12:00:00Z");
  private static final UUID TASK_ID = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
  private static final UUID AUTHOR_ID = UUID.fromString("00000000-1111-2222-3333-444444444444");

  @BeforeEach
  void setUp() {
    uuidSupplier = () -> UUID.fromString("ffffffff-eeee-dddd-cccc-bbbbbbbbbbbb");
    commentService =
        new CommentService(
            commentRepository,
            taskRepository,
            userDirectory,
            activityService,
            uuidSupplier,
            Clock.fixed(NOW, ZoneOffset.UTC));
    lenient()
        .when(taskRepository.findById(TASK_ID))
        .thenReturn(
            Optional.of(
                Task.create(
                    TASK_ID,
                    UUID.randomUUID(),
                    "Task",
                    null,
                    TaskPriority.MEDIUM,
                    TaskStatus.TODO,
                    null,
                    null,
                    AUTHOR_ID,
                    Clock.fixed(NOW, ZoneOffset.UTC)::instant)));
    lenient().when(userDirectory.existsById(AUTHOR_ID)).thenReturn(true);
  }

  @Test
  void addCommentPersistsComment() {
    ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);
    when(commentRepository.save(captor.capture()))
        .thenAnswer(invocation -> invocation.getArgument(0));

    when(activityService.record(any(), any(), any(), any(), any())).thenAnswer(invocation -> null);

    Comment comment = commentService.addComment(TASK_ID, AUTHOR_ID, " Great job ");

    assertThat(comment.body()).isEqualTo("Great job");
    assertThat(comment.createdAt()).isEqualTo(NOW);
    verify(commentRepository).save(any(Comment.class));
    ArgumentCaptor<Map<String, Object>> payloadCaptor = ArgumentCaptor.forClass(Map.class);
    verify(activityService)
        .record(
            eq(ActivityCodebook.ENTITY_COMMENT),
            eq(comment.id()),
            eq(AUTHOR_ID),
            eq(ActivityCodebook.ACTION_COMMENT_ADDED),
            payloadCaptor.capture());
    assertThat(payloadCaptor.getValue())
        .containsEntry("taskId", TASK_ID)
        .containsEntry("body", "Great job");
  }

  @Test
  void addCommentRejectsMissingTask() {
    when(taskRepository.findById(TASK_ID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> commentService.addComment(TASK_ID, AUTHOR_ID, "Body"))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("Task not found");
  }

  @Test
  void addCommentRejectsUnknownAuthor() {
    when(userDirectory.existsById(AUTHOR_ID)).thenReturn(false);

    assertThatThrownBy(() -> commentService.addComment(TASK_ID, AUTHOR_ID, "Body"))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("Author does not exist");
  }

  @Test
  void listCommentsDelegatesToRepository() {
    when(commentRepository.findByTaskId(TASK_ID)).thenReturn(List.of());

    assertThat(commentService.listComments(TASK_ID)).isEmpty();
    verify(commentRepository).findByTaskId(TASK_ID);
  }

  @Test
  void deleteCommentInvokesRepository() {
    UUID commentId = UUID.randomUUID();

    Comment existing = new Comment(commentId, TASK_ID, AUTHOR_ID, "body", NOW);
    when(commentRepository.findById(commentId)).thenReturn(Optional.of(existing));
    when(activityService.record(any(), any(), any(), any(), any())).thenAnswer(invocation -> null);

    boolean deleted = commentService.deleteComment(TASK_ID, commentId, AUTHOR_ID);

    assertThat(deleted).isTrue();
    verify(commentRepository).delete(commentId);
    ArgumentCaptor<Map<String, Object>> payloadCaptor = ArgumentCaptor.forClass(Map.class);
    verify(activityService)
        .record(
            eq(ActivityCodebook.ENTITY_COMMENT),
            eq(commentId),
            eq(AUTHOR_ID),
            eq(ActivityCodebook.ACTION_COMMENT_REMOVED),
            payloadCaptor.capture());
    assertThat(payloadCaptor.getValue()).containsEntry("taskId", TASK_ID);
  }

  @Test
  void deleteCommentWithNullActorThrows() {
    UUID commentId = UUID.randomUUID();
    Comment existing = new Comment(commentId, TASK_ID, AUTHOR_ID, "body", NOW);
    when(commentRepository.findById(commentId)).thenReturn(Optional.of(existing));

    assertThatThrownBy(() -> commentService.deleteComment(TASK_ID, commentId, null))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void deleteCommentNotFoundSkipsActivity() {
    UUID commentId = UUID.randomUUID();
    when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

    boolean deleted = commentService.deleteComment(TASK_ID, commentId, AUTHOR_ID);

    assertThat(deleted).isFalse();

    verify(commentRepository, never()).delete(commentId);
    verify(activityService, never())
        .record(any(), any(), any(), eq(ActivityCodebook.ACTION_COMMENT_REMOVED), any());
  }

  @Test
  void deleteCommentWithTaskMismatchReturnsFalse() {
    UUID commentId = UUID.randomUUID();
    UUID otherTaskId = UUID.randomUUID();
    Comment existing = new Comment(commentId, otherTaskId, AUTHOR_ID, "body", NOW);
    when(commentRepository.findById(commentId)).thenReturn(Optional.of(existing));

    boolean deleted = commentService.deleteComment(TASK_ID, commentId, AUTHOR_ID);

    assertThat(deleted).isFalse();
    verify(commentRepository, never()).delete(commentId);
    verify(activityService, never())
        .record(any(), any(), any(), eq(ActivityCodebook.ACTION_COMMENT_REMOVED), any());
  }
}
