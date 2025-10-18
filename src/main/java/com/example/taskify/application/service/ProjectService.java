package com.example.taskify.application.service;

import com.example.taskify.application.port.out.ProjectEventPublisher;
import com.example.taskify.application.port.out.ProjectRepository;
import com.example.taskify.application.port.out.UserDirectory;
import com.example.taskify.domain.project.Project;
import com.example.taskify.domain.project.ProjectStatus;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

@Service
public class ProjectService {

  private final ProjectRepository projectRepository;
  private final ProjectEventPublisher eventPublisher;
  private final UserDirectory userDirectory;
  private final Supplier<UUID> uuidSupplier;
  private final Clock clock;

  public ProjectService(
      ProjectRepository projectRepository,
      ProjectEventPublisher eventPublisher,
      UserDirectory userDirectory,
      Supplier<UUID> uuidSupplier,
      Clock clock) {
    this.projectRepository = projectRepository;
    this.eventPublisher = eventPublisher;
    this.userDirectory = userDirectory;
    this.uuidSupplier = uuidSupplier;
    this.clock = clock;
  }

  @Transactional
  public Project createProject(
      String name,
      String description,
      UUID ownerId,
      ProjectStatus status,
      LocalDate startDate,
      LocalDate endDate,
      UUID creatorId) {
    Assert.hasText(name, "name must not be blank");
    Assert.notNull(ownerId, "ownerId must not be null");
    Assert.notNull(creatorId, "creatorId must not be null");
    if (!userDirectory.existsById(ownerId)) {
      throw new IllegalArgumentException("Owner does not exist");
    }
    Project project =
        Project.create(
            uuidSupplier.get(),
            name,
            description,
            ownerId,
            status,
            startDate,
            endDate,
            creatorId,
            this::now);
    Project persisted = projectRepository.save(project);
    eventPublisher.publish(persisted.drainEvents());
    return persisted;
  }

  @Transactional
  public Project changeStatus(UUID projectId, ProjectStatus newStatus, UUID actorId) {
    Project project =
        projectRepository
            .findById(projectId)
            .orElseThrow(() -> new IllegalArgumentException("Project not found"));
    project.changeStatus(newStatus, actorId, this::now);
    Project persisted = projectRepository.save(project);
    eventPublisher.publish(persisted.drainEvents());
    return persisted;
  }

  @Transactional
  public Project updateDetails(
      UUID projectId,
      String name,
      String description,
      UUID actorId,
      LocalDate start,
      LocalDate end) {
    Project project =
        projectRepository
            .findById(projectId)
            .orElseThrow(() -> new IllegalArgumentException("Project not found"));
    project.rename(name, description, actorId, this::now);
    project.schedule(start, end, actorId, this::now);
    Project persisted = projectRepository.save(project);
    eventPublisher.publish(persisted.drainEvents());
    return persisted;
  }

  @Transactional
  public Project reassignOwner(UUID projectId, UUID newOwnerId, UUID actorId) {
    if (!userDirectory.existsById(newOwnerId)) {
      throw new IllegalArgumentException("Owner does not exist");
    }
    Project project =
        projectRepository
            .findById(projectId)
            .orElseThrow(() -> new IllegalArgumentException("Project not found"));
    project.changeOwner(newOwnerId, actorId, this::now);
    Project persisted = projectRepository.save(project);
    eventPublisher.publish(persisted.drainEvents());
    return persisted;
  }

  private Instant now() {
    return clock.instant();
  }

  @Transactional(readOnly = true)
  public Project getProject(UUID projectId) {
    return projectRepository
        .findById(projectId)
        .orElseThrow(() -> new IllegalArgumentException("Project not found"));
  }

  @Transactional(readOnly = true)
  public List<Project> listProjects() {
    return projectRepository.findAll();
  }
}
