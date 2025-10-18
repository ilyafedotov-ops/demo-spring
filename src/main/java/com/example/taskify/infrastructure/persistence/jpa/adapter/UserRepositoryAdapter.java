package com.example.taskify.infrastructure.persistence.jpa.adapter;

import com.example.taskify.application.port.out.UserRepository;
import com.example.taskify.domain.user.User;
import com.example.taskify.infrastructure.persistence.jpa.mapper.UserEntityMapper;
import com.example.taskify.infrastructure.persistence.jpa.repository.UserJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class UserRepositoryAdapter implements UserRepository {

  private final UserJpaRepository userJpaRepository;
  private final UserEntityMapper mapper;

  public UserRepositoryAdapter(UserJpaRepository userJpaRepository, UserEntityMapper mapper) {
    this.userJpaRepository = userJpaRepository;
    this.mapper = mapper;
  }

  @Override
  public Optional<User> findById(UUID id) {
    return userJpaRepository.findById(id).map(mapper::toDomain);
  }

  @Override
  public List<User> findAll() {
    return userJpaRepository.findAll().stream().map(mapper::toDomain).toList();
  }
}
