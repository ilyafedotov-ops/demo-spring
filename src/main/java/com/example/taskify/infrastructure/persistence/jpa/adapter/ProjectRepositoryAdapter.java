package com.example.taskify.infrastructure.persistence.jpa.adapter;

import com.example.taskify.application.port.out.ProjectRepository;
import com.example.taskify.domain.project.Project;
import com.example.taskify.infrastructure.persistence.jpa.entity.ProjectJpaEntity;
import com.example.taskify.infrastructure.persistence.jpa.mapper.ProjectEntityMapper;
import com.example.taskify.infrastructure.persistence.jpa.repository.ProjectJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class ProjectRepositoryAdapter implements ProjectRepository {

  private final ProjectJpaRepository jpaRepository;
  private final ProjectEntityMapper mapper;

  public ProjectRepositoryAdapter(ProjectJpaRepository jpaRepository, ProjectEntityMapper mapper) {
    this.jpaRepository = jpaRepository;
    this.mapper = mapper;
  }

  @Override
  public Project save(Project project) {
    ProjectJpaEntity entity = mapper.toEntity(project);
    jpaRepository.save(entity);
    return project;
  }

  @Override
  public Optional<Project> findById(UUID id) {
    return jpaRepository.findById(id).map(mapper::toDomain);
  }

  @Override
  public List<Project> findAll() {
    return jpaRepository.findAll().stream().map(mapper::toDomain).toList();
  }
}
