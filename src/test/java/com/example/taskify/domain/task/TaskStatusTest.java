package com.example.taskify.domain.task;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class TaskStatusTest {

  @ParameterizedTest
  @MethodSource("transitions")
  void validatesTransitions(TaskStatus current, TaskStatus target, boolean allowed) {
    assertThat(current.canTransitionTo(target)).isEqualTo(allowed);
  }

  private static Stream<Arguments> transitions() {
    return Stream.of(
        Arguments.of(TaskStatus.TODO, TaskStatus.IN_PROGRESS, true),
        Arguments.of(TaskStatus.TODO, TaskStatus.DONE, false),
        Arguments.of(TaskStatus.IN_PROGRESS, TaskStatus.DONE, true),
        Arguments.of(TaskStatus.IN_PROGRESS, TaskStatus.TODO, false),
        Arguments.of(TaskStatus.BLOCKED, TaskStatus.IN_PROGRESS, true),
        Arguments.of(TaskStatus.DONE, TaskStatus.CANCELLED, true),
        Arguments.of(TaskStatus.CANCELLED, TaskStatus.TODO, false));
  }
}
