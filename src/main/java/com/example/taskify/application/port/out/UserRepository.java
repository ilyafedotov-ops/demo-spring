package com.example.taskify.application.port.out;

import com.example.taskify.domain.user.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
  Optional<User> findById(UUID id);

  List<User> findAll();
}
