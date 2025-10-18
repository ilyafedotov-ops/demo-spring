package com.example.taskify.web;

import com.example.taskify.config.security.ActorHeaderAuthenticationFilter;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class ActorResolver {

  public UUID requireActor(Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      throw new ResponseStatusException(
          HttpStatus.UNAUTHORIZED, "Missing " + ActorHeaderAuthenticationFilter.ACTOR_HEADER);
    }
    try {
      return UUID.fromString(authentication.getName());
    } catch (IllegalArgumentException ex) {
      throw new ResponseStatusException(
          HttpStatus.UNAUTHORIZED, "Invalid " + ActorHeaderAuthenticationFilter.ACTOR_HEADER, ex);
    }
  }
}
