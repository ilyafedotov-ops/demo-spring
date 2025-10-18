package com.example.taskify.application.exception;

/**
 * Signals that a requested resource does not exist. Controllers map this to HTTP 404 responses via
 * {@link com.example.taskify.web.ApiErrorHandler}.
 */
public class ResourceNotFoundException extends RuntimeException {

  public ResourceNotFoundException(String message) {
    super(message);
  }
}
