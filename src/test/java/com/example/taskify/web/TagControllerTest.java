package com.example.taskify.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.taskify.application.service.TagService;
import com.example.taskify.config.security.SecurityConfig;
import com.example.taskify.domain.tag.Tag;
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

@WebMvcTest(TagController.class)
@Import({SecurityConfig.class, ActorResolver.class, ApiErrorHandler.class})
class TagControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @MockBean private TagService tagService;
  @MockBean private TagMapper tagMapper;

  private static final UUID TAG_ID = UUID.fromString("00000000-0000-0000-0000-000000000010");
  private static final UUID ACTOR_ID = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");

  @Test
  void createTagReturnsCreated() throws Exception {
    TagCreateRequest request = new TagCreateRequest("Backend", "#2563EB");
    Tag domain = Tag.create(TAG_ID, "Backend", "#2563EB", Instant.parse("2025-01-01T00:00:00Z"));
    TagResponse response = sampleResponse();

    when(tagService.createTag("Backend", "#2563EB")).thenReturn(domain);
    when(tagMapper.toResponse(domain)).thenReturn(response);

    mockMvc
        .perform(
            authenticated(post("/api/tags"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", "/api/tags/" + TAG_ID))
        .andExpect(jsonPath("$.id").value(TAG_ID.toString()))
        .andExpect(jsonPath("$.name").value("Backend"));

    verify(tagService).createTag("Backend", "#2563EB");
  }

  @Test
  void createTagReturnsBadRequestWhenDuplicate() throws Exception {
    TagCreateRequest request = new TagCreateRequest("Backend", "#2563EB");
    doThrow(new IllegalArgumentException("Tag with name already exists"))
        .when(tagService)
        .createTag(any(), any());

    mockMvc
        .perform(
            authenticated(post("/api/tags"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void listTagsReturnsResponses() throws Exception {
    Tag domain = Tag.create(TAG_ID, "Backend", "#2563EB", Instant.parse("2025-01-01T00:00:00Z"));
    TagResponse response = sampleResponse();

    when(tagService.listTags()).thenReturn(List.of(domain));
    when(tagMapper.toResponses(List.of(domain))).thenReturn(List.of(response));

    mockMvc
        .perform(authenticated(get("/api/tags")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(TAG_ID.toString()));
  }

  private MockHttpServletRequestBuilder authenticated(MockHttpServletRequestBuilder builder) {
    return builder.header("X-Actor-Id", ACTOR_ID.toString());
  }

  private TagResponse sampleResponse() {
    Instant now = Instant.parse("2025-01-01T00:00:00Z");
    return new TagResponse(TAG_ID, "Backend", "#2563EB", now, now);
  }
}
