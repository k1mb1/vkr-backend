package com.github.k1mb1.vkr_backend.common.error;

import static com.github.k1mb1.vkr_backend.common.error.ErrorMessages.*;
import static org.springframework.http.HttpStatus.*;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@Hidden
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorDto> handleEntityNotFound(
        EntityNotFoundException ex
    ) {
        log.warn("Entity not found: {}", ex.getMessage());
        return ResponseEntity.status(NOT_FOUND).body(
            ErrorDto.of(ErrorCode.NOT_FOUND, ex.getMessage(), NOT_FOUND)
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDto> handleValidation(
        MethodArgumentNotValidException ex
    ) {
        var fieldErrors = ex
            .getBindingResult()
            .getFieldErrors()
            .stream()
            .map(err ->
                new FieldError(
                    err.getField(),
                    err.getCode(),
                    err.getDefaultMessage()
                )
            )
            .toList();
        return ResponseEntity.status(BAD_REQUEST).body(
            ErrorDto.of(
                ErrorCode.VALIDATION_FAILED,
                VALIDATION_FAILED,
                BAD_REQUEST,
                fieldErrors
            )
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorDto> handleConstraintViolation(
        ConstraintViolationException ex
    ) {
        var fieldErrors = ex
            .getConstraintViolations()
            .stream()
            .map(v ->
                new FieldError(
                    v.getPropertyPath().toString(),
                    v
                        .getConstraintDescriptor()
                        .getAnnotation()
                        .annotationType()
                        .getSimpleName(),
                    v.getMessage()
                )
            )
            .toList();
        return ResponseEntity.status(BAD_REQUEST).body(
            ErrorDto.of(
                ErrorCode.VALIDATION_FAILED,
                VALIDATION_FAILED,
                BAD_REQUEST,
                fieldErrors
            )
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorDto> handleIllegalArgument(
        IllegalArgumentException ex
    ) {
        log.warn("Illegal argument: {}", ex.getMessage());
        return ResponseEntity.status(BAD_REQUEST).body(
            ErrorDto.of(
                ErrorCode.ILLEGAL_ARGUMENT,
                ex.getMessage(),
                BAD_REQUEST
            )
        );
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorDto> handleIllegalState(
        IllegalStateException ex
    ) {
        log.warn("Illegal state: {}", ex.getMessage());
        return ResponseEntity.status(BAD_REQUEST).body(
            ErrorDto.of(ErrorCode.ILLEGAL_STATE, ex.getMessage(), BAD_REQUEST)
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorDto> handleUnreadableBody(
        HttpMessageNotReadableException ex
    ) {
        log.warn("Unreadable request body: {}", ex.getMessage());
        return ResponseEntity.status(BAD_REQUEST).body(
            ErrorDto.of(ErrorCode.MALFORMED_BODY, MALFORMED_BODY, BAD_REQUEST)
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorDto> handleTypeMismatch(
        MethodArgumentTypeMismatchException ex
    ) {
        var expected =
            ex.getRequiredType() != null
                ? "expected: " + ex.getRequiredType().getSimpleName()
                : "invalid type";
        var fieldError = new FieldError(ex.getName(), "TypeMismatch", expected);
        return ResponseEntity.status(BAD_REQUEST).body(
            ErrorDto.of(
                ErrorCode.INVALID_PARAM,
                INVALID_PARAM.formatted(ex.getName()),
                BAD_REQUEST,
                List.of(fieldError)
            )
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorDto> handleAccessDenied(
        AccessDeniedException ex
    ) {
        log.warn("Access denied: {}", ex.getMessage());
        return ResponseEntity.status(FORBIDDEN).body(
            ErrorDto.of(ErrorCode.ACCESS_DENIED, ACCESS_DENIED, FORBIDDEN)
        );
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorDto> handleNoResource(
        NoResourceFoundException ex
    ) {
        return ResponseEntity.status(NOT_FOUND).body(
            ErrorDto.of(ErrorCode.NOT_FOUND, RESOURCE_NOT_FOUND, NOT_FOUND)
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorDto> handleDataIntegrity(
        DataIntegrityViolationException ex
    ) {
        log.warn("Data integrity violation: {}", ex.getMessage());
        return ResponseEntity.status(CONFLICT).body(
            ErrorDto.of(ErrorCode.CONFLICT, DATA_INTEGRITY_VIOLATION, CONFLICT)
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDto> handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(
            ErrorDto.of(
                ErrorCode.INTERNAL_ERROR,
                INTERNAL_ERROR,
                INTERNAL_SERVER_ERROR
            )
        );
    }
}
