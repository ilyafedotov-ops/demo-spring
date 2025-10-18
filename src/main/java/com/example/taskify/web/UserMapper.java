package com.example.taskify.web;

import com.example.taskify.domain.user.User;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

  @Mapping(target = "displayName", expression = "java(user.getDisplayName().orElse(null))")
  @Mapping(target = "lastLoginAt", expression = "java(user.getLastLoginAt().orElse(null))")
  UserResponse toResponse(User user);

  List<UserResponse> toResponses(List<User> users);
}
