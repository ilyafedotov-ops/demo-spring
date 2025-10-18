package com.example.taskify.web;

import com.example.taskify.domain.activity.ActivityEntry;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ActivityMapper {

  ActivityResponse toResponse(ActivityEntry entry);

  List<ActivityResponse> toResponses(List<ActivityEntry> entries);
}
