package com.example.taskify.web;

import com.example.taskify.application.service.ActivityQueryService;
import java.util.List;
import java.util.UUID;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/activity")
@Validated
public class ActivityController {

  private final ActivityQueryService activityQueryService;
  private final ActivityMapper activityMapper;

  public ActivityController(
      ActivityQueryService activityQueryService, ActivityMapper activityMapper) {
    this.activityQueryService = activityQueryService;
    this.activityMapper = activityMapper;
  }

  @GetMapping
  public List<ActivityResponse> listActivity(
      @RequestParam("entityType") String entityType,
      @RequestParam(value = "entityId", required = false) UUID entityId,
      @RequestParam(value = "limit", required = false, defaultValue = "20") int limit) {
    if (entityId != null) {
      return activityMapper.toResponses(activityQueryService.listByEntity(entityType, entityId));
    }
    return activityMapper.toResponses(activityQueryService.listRecent(entityType, limit));
  }
}
