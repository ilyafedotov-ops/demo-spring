package com.example.taskify.web;

import com.example.taskify.application.service.TagService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tags")
@Validated
public class TagController {

  private final TagService tagService;
  private final TagMapper tagMapper;
  private final ActorResolver actorResolver;

  public TagController(TagService tagService, TagMapper tagMapper, ActorResolver actorResolver) {
    this.tagService = tagService;
    this.tagMapper = tagMapper;
    this.actorResolver = actorResolver;
  }

  @PostMapping
  public ResponseEntity<TagResponse> createTag(
      @Valid @RequestBody TagCreateRequest request, Authentication authentication) {
    actorResolver.requireActor(authentication);
    var created = tagService.createTag(request.name(), request.color());
    TagResponse response = tagMapper.toResponse(created);
    URI location = URI.create("/api/tags/" + response.id());
    return ResponseEntity.created(location).body(response);
  }

  @GetMapping
  public List<TagResponse> listTags() {
    return tagMapper.toResponses(tagService.listTags());
  }
}
