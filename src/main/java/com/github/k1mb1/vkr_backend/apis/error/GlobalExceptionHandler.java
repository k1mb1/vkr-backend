package com.github.k1mb1.vkr_backend.apis.error;

import static org.springframework.http.HttpStatus.*;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorDto> handleNotFoundException(
            EntityNotFoundException ex
    ) {
        log.error("Not Implemented Exception: {}", ex.getMessage());
        return ResponseEntity.status(NOT_FOUND).body(
            ErrorDto.of(ex.getMessage(), NOT_FOUND)
        );
    }
}
