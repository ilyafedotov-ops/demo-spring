package com.example.taskify.application.port.out;

import com.example.taskify.domain.task.Task;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskRepository {
  Task save(Task task);

  Optional<Task> findById(UUID id);

  List<Task> findByProjectId(UUID projectId);
}
