package com.example.taskify.application.port.out;

import com.example.taskify.domain.project.Project;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectRepository {
  Project save(Project project);

  Optional<Project> findById(UUID id);

  List<Project> findAll();
}
