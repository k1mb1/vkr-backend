package com.github.k1mb1.vkr_backend.attendance.checkin.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.k1mb1.vkr_backend.attendance.checkin.CheckInSessionApi;
import com.github.k1mb1.vkr_backend.attendance.checkin.domain.CheckInSessionState;
import com.github.k1mb1.vkr_backend.attendance.checkin.web.requests.ConfirmCheckInRequest;
import com.github.k1mb1.vkr_backend.attendance.checkin.web.requests.StartCheckInRequest;
import com.github.k1mb1.vkr_backend.attendance.checkin.web.responses.CheckInAudienceScope;
import com.github.k1mb1.vkr_backend.attendance.checkin.web.responses.CheckInPreviewResponse;
import com.github.k1mb1.vkr_backend.attendance.checkin.web.responses.CheckInSessionResponse;
import com.github.k1mb1.vkr_backend.common.error.ResourceNotFoundException;
import com.github.k1mb1.vkr_backend.support.ControllerTestSupport;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;

class CheckInSessionControllerTest {

    CheckInSessionApi checkInSessionApi;
    RestTestClient client;

    @BeforeEach
    void setUp() {
        checkInSessionApi = mock(CheckInSessionApi.class);
        client = ControllerTestSupport.client(new CheckInSessionController(checkInSessionApi));
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private CheckInSessionResponse sessionResponse(UUID id, UUID scopeId) {
        var lessonId = UUID.randomUUID();
        var now = Instant.now();
        return new CheckInSessionResponse(
            id, lessonId, scopeId, false,
            Collections.<CheckInAudienceScope>emptyList(),
            now, 600, 300,
            now.plusSeconds(600), now.plusSeconds(900),
            null, null, CheckInSessionState.OPEN
        );
    }

    // -----------------------------------------------------------------------
    // POST /api/check-in-sessions — start 200
    // -----------------------------------------------------------------------

    @Test
    void start_returnsOk() {
        var sessionId = UUID.randomUUID();
        var scopeId = UUID.randomUUID();
        var response = sessionResponse(sessionId, scopeId);
        when(checkInSessionApi.start(any())).thenReturn(response);

        var body = new StartCheckInRequest(scopeId, 600, 300);

        client.post().uri("/api/check-in-sessions")
            .contentType(MediaType.APPLICATION_JSON)
            .body(body)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").isEqualTo(sessionId.toString())
            .jsonPath("$.lessonScopeId").isEqualTo(scopeId.toString())
            .jsonPath("$.state").isEqualTo("OPEN");
    }

    // -----------------------------------------------------------------------
    // POST /api/check-in-sessions — lesson scope not found → 404
    // -----------------------------------------------------------------------

    @Test
    void start_lessonScopeNotFound_returns404() {
        var scopeId = UUID.randomUUID();
        when(checkInSessionApi.start(any()))
            .thenThrow(new ResourceNotFoundException("LessonScope", scopeId));

        client.post().uri("/api/check-in-sessions")
            .contentType(MediaType.APPLICATION_JSON)
            .body(new StartCheckInRequest(scopeId, 600, 300))
            .exchange()
            .expectStatus().isNotFound();
    }

    // -----------------------------------------------------------------------
    // POST /api/check-in-sessions — active session already exists → 400
    // -----------------------------------------------------------------------

    @Test
    void start_activeSessionAlreadyExists_returns400() {
        var scopeId = UUID.randomUUID();
        when(checkInSessionApi.start(any()))
            .thenThrow(new IllegalStateException("Active check-in session already exists"));

        client.post().uri("/api/check-in-sessions")
            .contentType(MediaType.APPLICATION_JSON)
            .body(new StartCheckInRequest(scopeId, 600, 300))
            .exchange()
            .expectStatus().isBadRequest();
    }

    // -----------------------------------------------------------------------
    // GET /api/check-in-sessions/{id} — 200
    // -----------------------------------------------------------------------

    @Test
    void get_returnsOk() {
        var sessionId = UUID.randomUUID();
        var scopeId = UUID.randomUUID();
        var response = sessionResponse(sessionId, scopeId);
        when(checkInSessionApi.get(sessionId)).thenReturn(response);

        client.get().uri("/api/check-in-sessions/{id}", sessionId)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").isEqualTo(sessionId.toString());
    }

    // -----------------------------------------------------------------------
    // GET /api/check-in-sessions/{id} — not found → 404
    // -----------------------------------------------------------------------

    @Test
    void get_notFound_returns404() {
        var sessionId = UUID.randomUUID();
        when(checkInSessionApi.get(sessionId))
            .thenThrow(new ResourceNotFoundException("CheckInSession", sessionId));

        client.get().uri("/api/check-in-sessions/{id}", sessionId)
            .exchange()
            .expectStatus().isNotFound();
    }

    // -----------------------------------------------------------------------
    // GET /api/check-in-sessions?permissionId=... — list 200
    // -----------------------------------------------------------------------

    @Test
    void list_returnsOkWithSessions() {
        var permissionId = UUID.randomUUID();
        var sessionId = UUID.randomUUID();
        var scopeId = UUID.randomUUID();
        var response = sessionResponse(sessionId, scopeId);
        when(checkInSessionApi.list(any())).thenReturn(List.of(response));

        client.get().uri("/api/check-in-sessions?permissionId=" + permissionId)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$[0].id").isEqualTo(sessionId.toString());
    }

    // -----------------------------------------------------------------------
    // GET /api/check-in-sessions?permissionId=... — permission not found → 404
    // -----------------------------------------------------------------------

    @Test
    void list_permissionNotFound_returns404() {
        var permissionId = UUID.randomUUID();
        when(checkInSessionApi.list(any()))
            .thenThrow(new ResourceNotFoundException("TeacherSubjectPermission", permissionId));

        client.get().uri("/api/check-in-sessions?permissionId=" + permissionId)
            .exchange()
            .expectStatus().isNotFound();
    }

    // -----------------------------------------------------------------------
    // GET /api/check-in-sessions/{id}/preview — 200
    // -----------------------------------------------------------------------

    @Test
    void preview_returnsOk() {
        var sessionId = UUID.randomUUID();
        var scopeId = UUID.randomUUID();
        var sessionResp = sessionResponse(sessionId, scopeId);
        var preview = new CheckInPreviewResponse(sessionResp, List.of());
        when(checkInSessionApi.preview(sessionId)).thenReturn(preview);

        client.get().uri("/api/check-in-sessions/{id}/preview", sessionId)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.session.id").isEqualTo(sessionId.toString())
            .jsonPath("$.rows").isArray();
    }

    // -----------------------------------------------------------------------
    // GET /api/check-in-sessions/{id}/preview — not found → 404
    // -----------------------------------------------------------------------

    @Test
    void preview_notFound_returns404() {
        var sessionId = UUID.randomUUID();
        when(checkInSessionApi.preview(sessionId))
            .thenThrow(new ResourceNotFoundException("CheckInSession", sessionId));

        client.get().uri("/api/check-in-sessions/{id}/preview", sessionId)
            .exchange()
            .expectStatus().isNotFound();
    }

    // -----------------------------------------------------------------------
    // POST /api/check-in-sessions/{id}/confirm — 200
    // -----------------------------------------------------------------------

    @Test
    void confirm_returnsOk() {
        var sessionId = UUID.randomUUID();
        var scopeId = UUID.randomUUID();
        var confirmed = new CheckInSessionResponse(
            sessionId, UUID.randomUUID(), scopeId, false,
            Collections.emptyList(), Instant.now(), 600, 300,
            Instant.now().plusSeconds(600), Instant.now().plusSeconds(900),
            Instant.now(), null, CheckInSessionState.CONFIRMED
        );
        when(checkInSessionApi.confirm(eq(sessionId), any())).thenReturn(confirmed);

        client.post().uri("/api/check-in-sessions/{id}/confirm", sessionId)
            .contentType(MediaType.APPLICATION_JSON)
            .body(new ConfirmCheckInRequest(null))
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").isEqualTo(sessionId.toString())
            .jsonPath("$.state").isEqualTo("CONFIRMED");
    }

    // -----------------------------------------------------------------------
    // POST /api/check-in-sessions/{id}/confirm — already confirmed → 400
    // -----------------------------------------------------------------------

    @Test
    void confirm_alreadyConfirmed_returns400() {
        var sessionId = UUID.randomUUID();
        when(checkInSessionApi.confirm(eq(sessionId), any()))
            .thenThrow(new IllegalStateException("Session already confirmed"));

        client.post().uri("/api/check-in-sessions/{id}/confirm", sessionId)
            .contentType(MediaType.APPLICATION_JSON)
            .body(new ConfirmCheckInRequest(null))
            .exchange()
            .expectStatus().isBadRequest();
    }

    // -----------------------------------------------------------------------
    // POST /api/check-in-sessions/{id}/confirm — cancelled session → 400
    // -----------------------------------------------------------------------

    @Test
    void confirm_cancelledSession_returns400() {
        var sessionId = UUID.randomUUID();
        when(checkInSessionApi.confirm(eq(sessionId), any()))
            .thenThrow(new IllegalStateException("Session is cancelled"));

        client.post().uri("/api/check-in-sessions/{id}/confirm", sessionId)
            .contentType(MediaType.APPLICATION_JSON)
            .body(new ConfirmCheckInRequest(null))
            .exchange()
            .expectStatus().isBadRequest();
    }

    // -----------------------------------------------------------------------
    // POST /api/check-in-sessions/{id}/confirm — override student not in audience → 400
    // -----------------------------------------------------------------------

    @Test
    void confirm_overrideStudentNotInAudience_returns400() {
        var sessionId = UUID.randomUUID();
        when(checkInSessionApi.confirm(eq(sessionId), any()))
            .thenThrow(new IllegalArgumentException("Override references student not in lesson audience"));

        client.post().uri("/api/check-in-sessions/{id}/confirm", sessionId)
            .contentType(MediaType.APPLICATION_JSON)
            .body(new ConfirmCheckInRequest(null))
            .exchange()
            .expectStatus().isBadRequest();
    }

    // -----------------------------------------------------------------------
    // POST /api/check-in-sessions/{id}/cancel — 200
    // -----------------------------------------------------------------------

    @Test
    void cancel_returnsOk() {
        var sessionId = UUID.randomUUID();
        var scopeId = UUID.randomUUID();
        var cancelled = new CheckInSessionResponse(
            sessionId, UUID.randomUUID(), scopeId, false,
            Collections.emptyList(), Instant.now(), 600, 300,
            Instant.now().plusSeconds(600), Instant.now().plusSeconds(900),
            null, Instant.now(), CheckInSessionState.CANCELLED
        );
        when(checkInSessionApi.cancel(sessionId)).thenReturn(cancelled);

        client.post().uri("/api/check-in-sessions/{id}/cancel", sessionId)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").isEqualTo(sessionId.toString())
            .jsonPath("$.state").isEqualTo("CANCELLED");
    }

    // -----------------------------------------------------------------------
    // POST /api/check-in-sessions/{id}/cancel — confirmed session → 400
    // -----------------------------------------------------------------------

    @Test
    void cancel_confirmedSession_returns400() {
        var sessionId = UUID.randomUUID();
        when(checkInSessionApi.cancel(sessionId))
            .thenThrow(new IllegalStateException("Cannot cancel a confirmed session"));

        client.post().uri("/api/check-in-sessions/{id}/cancel", sessionId)
            .exchange()
            .expectStatus().isBadRequest();
    }
}
