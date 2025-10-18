package com.example.taskify.infrastructure.persistence.jpa.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.taskify.application.port.out.TagRepository;
import com.example.taskify.domain.tag.Tag;
import com.example.taskify.infrastructure.persistence.jpa.mapper.TagEntityMapper;
import com.example.taskify.infrastructure.persistence.jpa.repository.TagJpaRepository;
import com.example.taskify.support.PostgresIntegrationTest;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
  TagRepositoryAdapter.class,
  TagEntityMapper.class,
  com.example.taskify.config.TaskifyPropertiesConfiguration.class,
  com.example.taskify.config.PersistenceConfig.class
})
class TagRepositoryAdapterIntegrationTest extends PostgresIntegrationTest {

  @Autowired private TagRepository tagRepository;
  @Autowired private TagJpaRepository tagJpaRepository;

  @Test
  void saveAndRetrieveTags() {
    Instant now = Instant.now();
    Tag backend = Tag.create(UUID.randomUUID(), "Backend", "#2563EB", now);
    Tag frontend = Tag.create(UUID.randomUUID(), "Frontend", "#F97316", now.plusSeconds(5));

    tagRepository.save(backend);
    tagRepository.save(frontend);

    assertThat(tagJpaRepository.count()).isEqualTo(2);

    List<Tag> tags = tagRepository.findAll();
    assertThat(tags).extracting(Tag::getName).containsExactlyInAnyOrder("Backend", "Frontend");
    assertThat(tagRepository.existsByName("backend")).isTrue();
    assertThat(tagRepository.existsByName("unknown")).isFalse();
  }
}
