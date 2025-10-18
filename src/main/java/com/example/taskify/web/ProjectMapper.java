package com.example.taskify.web;

import com.example.taskify.domain.project.Project;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProjectMapper {

  @Mapping(target = "startDate", expression = "java(project.getStartDate().orElse(null))")
  @Mapping(target = "endDate", expression = "java(project.getEndDate().orElse(null))")
  @Mapping(target = "updatedBy", expression = "java(project.getUpdatedBy().orElse(null))")
  ProjectResponse toResponse(Project project);

  List<ProjectResponse> toResponses(List<Project> projects);
}
