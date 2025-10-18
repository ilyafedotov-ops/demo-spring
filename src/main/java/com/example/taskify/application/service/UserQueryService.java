package com.example.taskify.application.service;

import com.example.taskify.application.port.out.UserRepository;
import com.example.taskify.domain.user.User;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserQueryService {

  private final UserRepository userRepository;

  public UserQueryService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Transactional(readOnly = true)
  public User getUser(UUID userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("User not found"));
  }

  @Transactional(readOnly = true)
  public List<User> listUsers() {
    return userRepository.findAll();
  }
}
