package com.example.taskify.infrastructure.persistence.jpa.mapper;

import com.example.taskify.domain.tag.Tag;
import com.example.taskify.infrastructure.persistence.jpa.entity.TagJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class TagEntityMapper {

  public TagJpaEntity toEntity(Tag tag) {
    TagJpaEntity entity = new TagJpaEntity();
    entity.setId(tag.getId());
    entity.setName(tag.getName());
    entity.setColor(tag.getColor());
    entity.setCreatedAt(tag.getCreatedAt());
    entity.setUpdatedAt(tag.getUpdatedAt());
    return entity;
  }

  public Tag toDomain(TagJpaEntity entity) {
    return Tag.rehydrate(
        entity.getId(),
        entity.getName(),
        entity.getColor(),
        entity.getCreatedAt(),
        entity.getUpdatedAt());
  }
}
