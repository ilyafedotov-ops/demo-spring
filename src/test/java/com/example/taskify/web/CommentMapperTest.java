package com.example.taskify.web;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.taskify.domain.comment.Comment;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class CommentMapperTest {

  private final CommentMapper mapper = Mappers.getMapper(CommentMapper.class);

  @Test
  void toResponseCopiesFields() {
    Comment comment =
        new Comment(
            UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),
            UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"),
            UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc"),
            "Body",
            Instant.parse("2024-01-01T00:00:00Z"));

    CommentResponse response = mapper.toResponse(comment);

    assertThat(response.body()).isEqualTo("Body");
    assertThat(response.createdAt()).isEqualTo(Instant.parse("2024-01-01T00:00:00Z"));
  }

  @Test
  void toResponsesMapsList() {
    Comment comment =
        new Comment(
            UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd"),
            UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee"),
            UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff"),
            "Other",
            Instant.parse("2024-02-01T00:00:00Z"));

    List<CommentResponse> responses = mapper.toResponses(List.of(comment));

    assertThat(responses).hasSize(1);
    assertThat(responses.get(0).body()).isEqualTo("Other");
  }
}
