package com.example.taskify.web;

import com.example.taskify.application.service.UserQueryService;
import java.util.List;
import java.util.UUID;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@Validated
public class UserController {

  private final UserQueryService userQueryService;
  private final UserMapper userMapper;

  public UserController(UserQueryService userQueryService, UserMapper userMapper) {
    this.userQueryService = userQueryService;
    this.userMapper = userMapper;
  }

  @GetMapping
  public List<UserResponse> listUsers() {
    return userMapper.toResponses(userQueryService.listUsers());
  }

  @GetMapping("/{userId}")
  public UserResponse getUser(@PathVariable UUID userId) {
    return userMapper.toResponse(userQueryService.getUser(userId));
  }
}
