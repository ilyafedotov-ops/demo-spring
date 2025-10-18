package com.example.taskify.infrastructure.persistence.jpa.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.taskify.application.port.out.ProjectRepository;
import com.example.taskify.domain.project.Project;
import com.example.taskify.domain.project.ProjectStatus;
import com.example.taskify.domain.user.UserRole;
import com.example.taskify.domain.user.UserStatus;
import com.example.taskify.infrastructure.persistence.jpa.entity.UserJpaEntity;
import com.example.taskify.infrastructure.persistence.jpa.mapper.ProjectEntityMapper;
import com.example.taskify.infrastructure.persistence.jpa.repository.ProjectJpaRepository;
import com.example.taskify.infrastructure.persistence.jpa.repository.UserJpaRepository;
import com.example.taskify.support.PostgresIntegrationTest;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
  ProjectRepositoryAdapter.class,
  ProjectEntityMapper.class,
  DatabaseUserDirectory.class,
  com.example.taskify.config.TaskifyPropertiesConfiguration.class,
  com.example.taskify.config.PersistenceConfig.class
})
class ProjectRepositoryAdapterIntegrationTest extends PostgresIntegrationTest {

  @Autowired private ProjectRepository projectRepository;
  @Autowired private ProjectJpaRepository projectJpaRepository;
  @Autowired private UserJpaRepository userJpaRepository;

  private UUID ownerId;

  @BeforeEach
  void setUp() {
    ownerId = UUID.randomUUID();
    Instant now = Instant.now();

    UserJpaEntity user = new UserJpaEntity();
    user.setId(ownerId);
    user.setUsername("lead");
    user.setEmail("lead@example.com");
    user.setPasswordHash("hashed");
    user.setDisplayName("Lead");
    user.setRole(UserRole.TEAM_LEAD);
    user.setStatus(UserStatus.ACTIVE);
    user.setLastLoginAt(now);
    user.setCreatedAt(now);
    user.setCreatedBy(ownerId);
    user.setUpdatedAt(now);
    user.setUpdatedBy(ownerId);
    userJpaRepository.save(user);
  }

  @Test
  void savesAndReadsProject() {
    UUID projectId = UUID.randomUUID();
    Project project =
        Project.create(
            projectId,
            "Platform",
            "Platform build",
            ownerId,
            ProjectStatus.PLANNED,
            LocalDate.of(2025, 4, 1),
            LocalDate.of(2025, 12, 31),
            ownerId,
            Instant::now);

    projectRepository.save(project);
    assertThat(projectJpaRepository.existsById(projectId)).isTrue();

    Project reloaded = projectRepository.findById(projectId).orElseThrow();
    assertThat(reloaded.getName()).isEqualTo("Platform");
    assertThat(reloaded.getOwnerId()).isEqualTo(ownerId);
    assertThat(reloaded.getStatus()).isEqualTo(ProjectStatus.PLANNED);
    assertThat(reloaded.getStartDate()).contains(LocalDate.of(2025, 4, 1));
  }

  @Test
  void findAllReturnsAllProjects() {
    Project first =
        Project.create(
            UUID.randomUUID(),
            "First",
            "",
            ownerId,
            ProjectStatus.PLANNED,
            LocalDate.of(2025, 1, 1),
            LocalDate.of(2025, 3, 31),
            ownerId,
            Instant::now);
    Project second =
        Project.create(
            UUID.randomUUID(),
            "Second",
            "",
            ownerId,
            ProjectStatus.ACTIVE,
            LocalDate.of(2025, 4, 1),
            LocalDate.of(2025, 6, 30),
            ownerId,
            Instant::now);

    projectRepository.save(first);
    projectRepository.save(second);

    assertThat(projectRepository.findAll())
        .extracting(Project::getName)
        .contains("First", "Second");
  }
}
