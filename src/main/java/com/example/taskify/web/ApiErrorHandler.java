package com.example.taskify.web;

import com.example.taskify.application.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiErrorHandler {

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ProblemDetail> handleIllegalArgument(IllegalArgumentException ex) {
    HttpStatus status = HttpStatus.BAD_REQUEST;
    ProblemDetail body = ProblemDetail.forStatusAndDetail(status, detail(ex.getMessage()));
    return ResponseEntity.status(status).body(body);
  }

  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<ProblemDetail> handleIllegalState(IllegalStateException ex) {
    HttpStatus status = HttpStatus.CONFLICT;
    ProblemDetail body = ProblemDetail.forStatusAndDetail(status, detail(ex.getMessage()));
    return ResponseEntity.status(status).body(body);
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ProblemDetail> handleNotFound(ResourceNotFoundException ex) {
    HttpStatus status = HttpStatus.NOT_FOUND;
    ProblemDetail body = ProblemDetail.forStatusAndDetail(status, detail(ex.getMessage()));
    return ResponseEntity.status(status).body(body);
  }

  private String detail(String message) {
    return message == null || message.isBlank() ? "Request could not be processed" : message;
  }
}
