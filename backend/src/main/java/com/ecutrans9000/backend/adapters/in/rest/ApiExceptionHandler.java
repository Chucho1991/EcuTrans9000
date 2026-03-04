package com.ecutrans9000.backend.adapters.in.rest;

import com.ecutrans9000.backend.service.BusinessException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Componente publico de backend para ApiExceptionHandler.
 */
@RestControllerAdvice
@Slf4j
public class ApiExceptionHandler {

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<Map<String, Object>> handleBusiness(BusinessException ex) {
    return build(ex.getStatus(), ex.getMessage(), null);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
    Map<String, String> errors = new HashMap<>();
    for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
      errors.put(fieldError.getField(), fieldError.getDefaultMessage());
    }
    return build(HttpStatus.BAD_REQUEST, "Datos de entrada invalidos", errors);
  }

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public ResponseEntity<Map<String, Object>> handleMaxUploadSize(MaxUploadSizeExceededException ex) {
    return build(HttpStatus.BAD_REQUEST, "Archivo excede tamano maximo permitido", null);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
    log.error("Error no controlado en API", ex);
    return build(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor", null);
  }

  private ResponseEntity<Map<String, Object>> build(HttpStatus status, String message, Object details) {
    Map<String, Object> body = new HashMap<>();
    body.put("timestamp", LocalDateTime.now().toString());
    body.put("status", status.value());
    body.put("message", message);
    if (details != null) {
      body.put("details", details);
    }
    return ResponseEntity.status(status).body(body);
  }
}
