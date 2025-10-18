package com.example.taskify.web;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.taskify.application.service.CommentService;
import com.example.taskify.config.security.SecurityConfig;
import com.example.taskify.domain.comment.Comment;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@WebMvcTest(CommentController.class)
@Import({SecurityConfig.class, ActorResolver.class, ApiErrorHandler.class})
class CommentControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @MockBean private CommentService commentService;
  @MockBean private CommentMapper commentMapper;

  private static final UUID TASK_ID = UUID.fromString("00000000-0000-0000-0000-000000000100");
  private static final UUID COMMENT_ID = UUID.fromString("00000000-0000-0000-0000-000000000101");
  private static final UUID ACTOR_ID = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");

  @Test
  void addCommentReturnsCreated() throws Exception {
    CommentCreateRequest request = new CommentCreateRequest("Great job!");
    Comment domain = sampleComment();
    CommentResponse response = sampleResponse();

    doReturn(domain).when(commentService).addComment(TASK_ID, ACTOR_ID, "Great job!");
    doReturn(response).when(commentMapper).toResponse(domain);

    mockMvc
        .perform(
            authenticated(post("/api/tasks/{taskId}/comments", TASK_ID))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", "/api/tasks/" + TASK_ID + "/comments/" + COMMENT_ID))
        .andExpect(jsonPath("$.id").value(COMMENT_ID.toString()))
        .andExpect(jsonPath("$.body").value("Great job!"));

    verify(commentService).addComment(TASK_ID, ACTOR_ID, "Great job!");
  }

  @Test
  void listCommentsReturnsResponses() throws Exception {
    Comment domain = sampleComment();
    CommentResponse response = sampleResponse();

    doReturn(List.of(domain)).when(commentService).listComments(TASK_ID);
    doReturn(List.of(response)).when(commentMapper).toResponses(List.of(domain));

    mockMvc
        .perform(authenticated(get("/api/tasks/{taskId}/comments", TASK_ID)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(COMMENT_ID.toString()));
  }

  @Test
  void deleteCommentReturnsNoContent() throws Exception {
    doReturn(true).when(commentService).deleteComment(COMMENT_ID, ACTOR_ID);

    mockMvc
        .perform(
            authenticated(delete("/api/tasks/{taskId}/comments/{commentId}", TASK_ID, COMMENT_ID)))
        .andExpect(status().isNoContent());

    verify(commentService).deleteComment(COMMENT_ID, ACTOR_ID);
  }

  @Test
  void deleteCommentReturnsNotFoundWhenMissing() throws Exception {
    doReturn(false).when(commentService).deleteComment(COMMENT_ID, ACTOR_ID);

    mockMvc
        .perform(
            authenticated(delete("/api/tasks/{taskId}/comments/{commentId}", TASK_ID, COMMENT_ID)))
        .andExpect(status().isNotFound());
  }

  @Test
  void addCommentReturnsBadRequestWhenTaskMissing() throws Exception {
    CommentCreateRequest request = new CommentCreateRequest("Great job!");
    doThrow(new IllegalArgumentException("Task not found"))
        .when(commentService)
        .addComment(TASK_ID, ACTOR_ID, "Great job!");

    mockMvc
        .perform(
            authenticated(post("/api/tasks/{taskId}/comments", TASK_ID))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNotFound());
  }

  private MockHttpServletRequestBuilder authenticated(MockHttpServletRequestBuilder builder) {
    return builder.header("X-Actor-Id", ACTOR_ID.toString());
  }

  private Comment sampleComment() {
    return new Comment(
        COMMENT_ID, TASK_ID, ACTOR_ID, "Great job!", Instant.parse("2025-01-01T00:00:00Z"));
  }

  private CommentResponse sampleResponse() {
    return new CommentResponse(
        COMMENT_ID, TASK_ID, ACTOR_ID, "Great job!", Instant.parse("2025-01-01T00:00:00Z"));
  }
}
