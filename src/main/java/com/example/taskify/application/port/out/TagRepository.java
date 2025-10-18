package com.example.taskify.application.port.out;

import com.example.taskify.domain.tag.Tag;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TagRepository {
  Tag save(Tag tag);

  Optional<Tag> findById(UUID id);

  List<Tag> findAll();

  boolean existsByName(String name);
}
