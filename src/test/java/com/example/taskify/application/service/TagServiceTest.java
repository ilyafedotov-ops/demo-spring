package com.example.taskify.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.taskify.application.port.out.TagRepository;
import com.example.taskify.domain.tag.Tag;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {

  @Mock private TagRepository tagRepository;

  private TagService tagService;
  private Supplier<UUID> uuidSupplier;
  private static final Instant NOW = Instant.parse("2025-01-01T00:00:00Z");

  @BeforeEach
  void setUp() {
    uuidSupplier = () -> UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
    tagService = new TagService(tagRepository, uuidSupplier, Clock.fixed(NOW, ZoneOffset.UTC));
  }

  @Test
  void createTagPersistsAndReturnsTag() {
    when(tagRepository.existsByName("Infra")).thenReturn(false);
    ArgumentCaptor<Tag> tagCaptor = ArgumentCaptor.forClass(Tag.class);
    when(tagRepository.save(tagCaptor.capture()))
        .thenAnswer(invocation -> invocation.getArgument(0));

    Tag tag = tagService.createTag("Infra", "#111111");

    assertThat(tag.getId()).isEqualTo(uuidSupplier.get());
    assertThat(tag.getColor()).isEqualTo("#111111");
    assertThat(tag.getCreatedAt()).isEqualTo(NOW);
    verify(tagRepository).save(tagCaptor.getValue());
  }

  @Test
  void createTagRejectsDuplicateNames() {
    when(tagRepository.existsByName("Infra")).thenReturn(true);

    assertThatThrownBy(() -> tagService.createTag("Infra", "#111111"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("already exists");
  }

  @Test
  void listTagsReturnsRepositoryResult() {
    when(tagRepository.findAll()).thenReturn(List.of());

    assertThat(tagService.listTags()).isEmpty();
    verify(tagRepository).findAll();
  }
}
