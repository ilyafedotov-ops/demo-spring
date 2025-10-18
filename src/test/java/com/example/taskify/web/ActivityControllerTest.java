package com.example.taskify.web;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.taskify.application.service.ActivityQueryService;
import com.example.taskify.config.security.SecurityConfig;
import com.example.taskify.domain.activity.ActivityEntry;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@WebMvcTest(ActivityController.class)
@Import({SecurityConfig.class, ActorResolver.class, ApiErrorHandler.class})
class ActivityControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @MockBean private ActivityQueryService activityQueryService;
  @MockBean private ActivityMapper activityMapper;

  private static final UUID ENTITY_ID = UUID.fromString("00000000-0000-0000-0000-00000000ABCD");

  @Test
  void listByEntityReturnsResponses() throws Exception {
    ActivityEntry entry =
        new ActivityEntry(
            UUID.randomUUID(),
            "TASK",
            ENTITY_ID,
            UUID.randomUUID(),
            "ACTION",
            Map.of("field", "value"),
            Instant.parse("2025-01-01T00:00:00Z"));
    ActivityResponse response =
        new ActivityResponse(
            entry.id(),
            entry.entityType(),
            entry.entityId(),
            entry.actorId(),
            entry.action(),
            entry.payload(),
            entry.occurredAt());

    when(activityQueryService.listByEntity("TASK", ENTITY_ID)).thenReturn(List.of(entry));
    when(activityMapper.toResponses(List.of(entry))).thenReturn(List.of(response));

    mockMvc
        .perform(
            authenticated(
                get("/api/activity")
                    .param("entityType", "TASK")
                    .param("entityId", ENTITY_ID.toString())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].entityType").value("TASK"));

    verify(activityQueryService).listByEntity("TASK", ENTITY_ID);
  }

  @Test
  void listRecentReturnsResponses() throws Exception {
    ActivityEntry entry =
        new ActivityEntry(
            UUID.randomUUID(),
            "TASK",
            ENTITY_ID,
            UUID.randomUUID(),
            "ACTION",
            Map.of(),
            Instant.parse("2025-01-02T00:00:00Z"));
    ActivityResponse response =
        new ActivityResponse(
            entry.id(),
            entry.entityType(),
            entry.entityId(),
            entry.actorId(),
            entry.action(),
            entry.payload(),
            entry.occurredAt());

    when(activityQueryService.listRecent(eq("TASK"), anyInt())).thenReturn(List.of(entry));
    when(activityMapper.toResponses(List.of(entry))).thenReturn(List.of(response));

    mockMvc
        .perform(
            authenticated(get("/api/activity").param("entityType", "TASK").param("limit", "5")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].action").value("ACTION"));

    verify(activityQueryService).listRecent("TASK", 5);
  }

  private MockHttpServletRequestBuilder authenticated(MockHttpServletRequestBuilder builder) {
    return builder.header("X-Actor-Id", UUID.randomUUID().toString());
  }
}
