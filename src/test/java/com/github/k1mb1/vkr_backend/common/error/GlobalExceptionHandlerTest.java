package com.github.k1mb1.vkr_backend.common.error;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;

/**
 * Unit-тесты обработчика ошибок: каждый @ExceptionHandler мапит исключение в
 * нужный HTTP-статус и {@link ErrorCode}. Тестируется напрямую, без поднятия MVC.
 */
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void entityNotFoundMapsTo404() {
        var response = handler.handleEntityNotFound(
            new ResourceNotFoundException("Teacher", UUID.randomUUID()));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo(ErrorCode.NOT_FOUND);
        assertThat(response.getBody().status()).isEqualTo(404);
        assertThat(response.getBody().message()).contains("Teacher");
    }

    @Test
    void illegalArgumentMapsTo400() {
        var response = handler.handleIllegalArgument(new IllegalArgumentException("bad input"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().code()).isEqualTo(ErrorCode.ILLEGAL_ARGUMENT);
        assertThat(response.getBody().message()).isEqualTo("bad input");
    }

    @Test
    void illegalStateMapsTo400() {
        var response = handler.handleIllegalState(new IllegalStateException("bad state"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().code()).isEqualTo(ErrorCode.ILLEGAL_STATE);
    }

    @Test
    void unreadableBodyMapsTo400() {
        var response = handler.handleUnreadableBody(
            new HttpMessageNotReadableException("nope", (HttpInputMessage) null));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().code()).isEqualTo(ErrorCode.MALFORMED_BODY);
    }

    @Test
    void accessDeniedMapsTo403() {
        var response = handler.handleAccessDenied(new AccessDeniedException("denied"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody().code()).isEqualTo(ErrorCode.ACCESS_DENIED);
    }

    @Test
    void dataIntegrityMapsTo409() {
        var response = handler.handleDataIntegrity(
            new DataIntegrityViolationException("constraint"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().code()).isEqualTo(ErrorCode.CONFLICT);
    }

    @Test
    void unexpectedMapsTo500() {
        var response = handler.handleUnexpected(new RuntimeException("boom"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().code()).isEqualTo(ErrorCode.INTERNAL_ERROR);
    }
}
