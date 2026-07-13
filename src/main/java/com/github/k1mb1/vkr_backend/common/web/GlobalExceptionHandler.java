package com.github.k1mb1.vkr_backend.common.web;

import static com.github.k1mb1.vkr_backend.common.web.ErrorMessages.ACCESS_DENIED;
import static com.github.k1mb1.vkr_backend.common.web.ErrorMessages.DATA_INTEGRITY_VIOLATION;
import static com.github.k1mb1.vkr_backend.common.web.ErrorMessages.INTERNAL_ERROR;
import static com.github.k1mb1.vkr_backend.common.web.ErrorMessages.INVALID_PARAM;
import static com.github.k1mb1.vkr_backend.common.web.ErrorMessages.MALFORMED_BODY;
import static com.github.k1mb1.vkr_backend.common.web.ErrorMessages.RESOURCE_NOT_FOUND;
import static com.github.k1mb1.vkr_backend.common.web.ErrorMessages.VALIDATION_FAILED;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import com.github.k1mb1.vkr_backend.common.exception.ConflictException;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@Hidden
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(NOT_FOUND)
    public ErrorDto handleEntityNotFound(EntityNotFoundException ex) {
        log.warn("Entity not found: {}", ex.getMessage());
        return ErrorDto.of(ErrorCode.NOT_FOUND, ex.getMessage(), NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(BAD_REQUEST)
    public ErrorDto handleValidation(MethodArgumentNotValidException ex) {
        var fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> new FieldError(err.getField(), err.getCode(), err.getDefaultMessage()))
                .toList();
        return ErrorDto.of(ErrorCode.VALIDATION_FAILED, VALIDATION_FAILED, BAD_REQUEST, fieldErrors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(BAD_REQUEST)
    public ErrorDto handleConstraintViolation(ConstraintViolationException ex) {
        var fieldErrors = ex.getConstraintViolations().stream()
                .map(v -> new FieldError(
                        v.getPropertyPath().toString(),
                        v.getConstraintDescriptor()
                                .getAnnotation()
                                .annotationType()
                                .getSimpleName(),
                        v.getMessage()))
                .toList();
        return ErrorDto.of(ErrorCode.VALIDATION_FAILED, VALIDATION_FAILED, BAD_REQUEST, fieldErrors);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(BAD_REQUEST)
    public ErrorDto handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Illegal argument: {}", ex.getMessage());
        return ErrorDto.of(ErrorCode.ILLEGAL_ARGUMENT, ex.getMessage(), BAD_REQUEST);
    }

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(CONFLICT)
    public ErrorDto handleConflict(ConflictException ex) {
        log.warn("Conflict: {}", ex.getMessage());
        return ErrorDto.of(ErrorCode.CONFLICT, ex.getMessage(), CONFLICT);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(BAD_REQUEST)
    public ErrorDto handleUnreadableBody(HttpMessageNotReadableException ex) {
        log.warn("Unreadable request body: {}", ex.getMessage());
        return ErrorDto.of(ErrorCode.MALFORMED_BODY, MALFORMED_BODY, BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(BAD_REQUEST)
    public ErrorDto handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        var expected = ex.getRequiredType() != null
                ? "expected: " + ex.getRequiredType().getSimpleName()
                : "invalid type";
        var fieldError = new FieldError(ex.getName(), "TypeMismatch", expected);
        return ErrorDto.of(
                ErrorCode.INVALID_PARAM, INVALID_PARAM.formatted(ex.getName()), BAD_REQUEST, List.of(fieldError));
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(FORBIDDEN)
    public ErrorDto handleAccessDenied(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        return ErrorDto.of(ErrorCode.ACCESS_DENIED, ACCESS_DENIED, FORBIDDEN);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(NOT_FOUND)
    public ErrorDto handleNoResource(NoResourceFoundException ex) {
        return ErrorDto.of(ErrorCode.NOT_FOUND, RESOURCE_NOT_FOUND, NOT_FOUND);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(CONFLICT)
    public ErrorDto handleDataIntegrity(DataIntegrityViolationException ex) {
        log.warn("Data integrity violation: {}", ex.getMessage());
        return ErrorDto.of(ErrorCode.CONFLICT, DATA_INTEGRITY_VIOLATION, CONFLICT);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    public ErrorDto handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);
        return ErrorDto.of(ErrorCode.INTERNAL_ERROR, INTERNAL_ERROR, INTERNAL_SERVER_ERROR);
    }
}
