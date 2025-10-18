package com.example.taskify.infrastructure.persistence.jpa.adapter;

import com.example.taskify.application.port.out.TagRepository;
import com.example.taskify.domain.tag.Tag;
import com.example.taskify.infrastructure.persistence.jpa.mapper.TagEntityMapper;
import com.example.taskify.infrastructure.persistence.jpa.repository.TagJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class TagRepositoryAdapter implements TagRepository {

  private final TagJpaRepository jpaRepository;
  private final TagEntityMapper mapper;

  public TagRepositoryAdapter(TagJpaRepository jpaRepository, TagEntityMapper mapper) {
    this.jpaRepository = jpaRepository;
    this.mapper = mapper;
  }

  @Override
  public Tag save(Tag tag) {
    jpaRepository.save(mapper.toEntity(tag));
    return tag;
  }

  @Override
  public Optional<Tag> findById(UUID id) {
    return jpaRepository.findById(id).map(mapper::toDomain);
  }

  @Override
  public List<Tag> findAll() {
    return jpaRepository.findAll().stream().map(mapper::toDomain).toList();
  }

  @Override
  public boolean existsByName(String name) {
    return jpaRepository.existsByNameIgnoreCase(name);
  }
}
