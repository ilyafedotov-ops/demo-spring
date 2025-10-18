package com.example.taskify.application.service;

import com.example.taskify.application.port.out.TagRepository;
import com.example.taskify.domain.tag.Tag;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

@Service
public class TagService {

  private final TagRepository tagRepository;
  private final Supplier<UUID> uuidSupplier;
  private final Clock clock;

  public TagService(TagRepository tagRepository, Supplier<UUID> uuidSupplier, Clock clock) {
    this.tagRepository = tagRepository;
    this.uuidSupplier = uuidSupplier;
    this.clock = clock;
  }

  @Transactional
  public Tag createTag(String name, String color) {
    Assert.hasText(name, "name must not be blank");
    Assert.hasText(color, "color must not be blank");
    if (tagRepository.existsByName(name)) {
      throw new IllegalArgumentException("Tag with name already exists");
    }
    Instant now = clock.instant();
    Tag tag = Tag.create(uuidSupplier.get(), name, color, now);
    return tagRepository.save(tag);
  }

  @Transactional(readOnly = true)
  public List<Tag> listTags() {
    return tagRepository.findAll();
  }
}
