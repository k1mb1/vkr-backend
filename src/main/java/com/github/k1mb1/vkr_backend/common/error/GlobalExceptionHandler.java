package com.github.k1mb1.vkr_backend.common.error;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

import static com.github.k1mb1.vkr_backend.common.error.ErrorMessages.*;
import static org.springframework.http.HttpStatus.*;

@Slf4j
@Hidden
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorDto> handleEntityNotFound(
        EntityNotFoundException ex
    ) {
        log.warn("Entity not found: {}", ex.getMessage());
        return ResponseEntity.status(NOT_FOUND).body(ErrorDto.of(ex.getMessage(), NOT_FOUND));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDto> handleValidation(
        MethodArgumentNotValidException ex
    ) {
        String details = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining("; "));
        return ResponseEntity.status(BAD_REQUEST)
            .body(ErrorDto.of(VALIDATION_FAILED, BAD_REQUEST, details));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorDto> handleConstraintViolation(
        ConstraintViolationException ex
    ) {
        String details = ex.getConstraintViolations()
            .stream()
            .map(v -> v.getPropertyPath() + ": " + v.getMessage())
            .collect(Collectors.joining("; "));
        return ResponseEntity.status(BAD_REQUEST)
            .body(ErrorDto.of(VALIDATION_FAILED, BAD_REQUEST, details));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorDto> handleIllegalArgument(
        IllegalArgumentException ex
    ) {
        log.warn("Illegal argument: {}", ex.getMessage());
        return ResponseEntity.status(BAD_REQUEST).body(ErrorDto.of(ex.getMessage(), BAD_REQUEST));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorDto> handleIllegalState(
        IllegalStateException ex
    ) {
        log.warn("Illegal state: {}", ex.getMessage());
        return ResponseEntity.status(BAD_REQUEST).body(ErrorDto.of(ex.getMessage(), BAD_REQUEST));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorDto> handleUnreadableBody(
        HttpMessageNotReadableException ex
    ) {
        log.warn("Unreadable request body: {}", ex.getMessage());
        return ResponseEntity.status(BAD_REQUEST).body(ErrorDto.of(MALFORMED_BODY, BAD_REQUEST));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorDto> handleTypeMismatch(
        MethodArgumentTypeMismatchException ex
    ) {
        String details = ex.getRequiredType() != null
                         ? "expected: " + ex.getRequiredType().getSimpleName()
                         : null;
        String message = INVALID_PARAM.formatted(ex.getName());
        return details != null
               ? ResponseEntity.status(BAD_REQUEST).body(ErrorDto.of(message, BAD_REQUEST, details))
               : ResponseEntity.status(BAD_REQUEST).body(ErrorDto.of(message, BAD_REQUEST));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorDto> handleAccessDenied(
        AccessDeniedException ex
    ) {
        log.warn("Access denied: {}", ex.getMessage());
        return ResponseEntity.status(FORBIDDEN).body(ErrorDto.of(ACCESS_DENIED, FORBIDDEN));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorDto> handleNoResource(
        NoResourceFoundException ex
    ) {
        return ResponseEntity.status(NOT_FOUND).body(ErrorDto.of(RESOURCE_NOT_FOUND, NOT_FOUND));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDto> handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity.status(INTERNAL_SERVER_ERROR)
            .body(ErrorDto.of(INTERNAL_ERROR, INTERNAL_SERVER_ERROR));
    }
}
