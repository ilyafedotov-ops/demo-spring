package com.example.taskify.web;

import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiErrorHandler {

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ProblemDetail> handleIllegalArgument(IllegalArgumentException ex) {
    HttpStatus status = deriveStatus(ex.getMessage());
    ProblemDetail body = ProblemDetail.forStatusAndDetail(status, detail(ex.getMessage()));
    return ResponseEntity.status(status).body(body);
  }

  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<ProblemDetail> handleIllegalState(IllegalStateException ex) {
    HttpStatus status = HttpStatus.CONFLICT;
    ProblemDetail body = ProblemDetail.forStatusAndDetail(status, detail(ex.getMessage()));
    return ResponseEntity.status(status).body(body);
  }

  private HttpStatus deriveStatus(String message) {
    if (message == null) {
      return HttpStatus.BAD_REQUEST;
    }
    String normalised = message.toLowerCase(Locale.ROOT);
    if (normalised.contains("not found")) {
      return HttpStatus.NOT_FOUND;
    }
    if (normalised.contains("does not exist")) {
      return HttpStatus.NOT_FOUND;
    }
    return HttpStatus.BAD_REQUEST;
  }

  private String detail(String message) {
    return message == null || message.isBlank() ? "Request could not be processed" : message;
  }
}
