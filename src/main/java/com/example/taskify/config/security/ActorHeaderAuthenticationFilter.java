package com.example.taskify.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

public class ActorHeaderAuthenticationFilter extends OncePerRequestFilter {

  public static final String ACTOR_HEADER = "X-Actor-Id";
  private static final String API_PREFIX = "/api/";

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    if (!request.getRequestURI().startsWith(API_PREFIX)) {
      filterChain.doFilter(request, response);
      return;
    }

    if (SecurityContextHolder.getContext().getAuthentication() != null) {
      filterChain.doFilter(request, response);
      return;
    }

    String actorHeader = request.getHeader(ACTOR_HEADER);
    if (!StringUtils.hasText(actorHeader)) {
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing X-Actor-Id header");
      return;
    }

    UUID actorId;
    try {
      actorId = UUID.fromString(actorHeader);
    } catch (IllegalArgumentException ex) {
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid X-Actor-Id header");
      return;
    }

    UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken(
            actorId.toString(), null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
    SecurityContextHolder.getContext().setAuthentication(authentication);
    try {
      filterChain.doFilter(request, response);
    } finally {
      SecurityContextHolder.clearContext();
    }
  }
}
