package com.example.taskify.web;

import com.example.taskify.domain.tag.Tag;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TagMapper {

  TagResponse toResponse(Tag tag);

  List<TagResponse> toResponses(List<Tag> tags);
}
