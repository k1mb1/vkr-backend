package com.github.k1mb1.vkr_backend.attendance.checkin.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.k1mb1.vkr_backend.attendance.checkin.CheckInRecordsApi;
import com.github.k1mb1.vkr_backend.attendance.checkin.CheckInSessionApi;
import com.github.k1mb1.vkr_backend.attendance.checkin.domain.CheckInRecordStatus;
import com.github.k1mb1.vkr_backend.attendance.checkin.domain.CheckInSessionState;
import com.github.k1mb1.vkr_backend.attendance.checkin.web.requests.StudentCheckInRequest;
import com.github.k1mb1.vkr_backend.attendance.checkin.web.responses.CheckInAudienceScope;
import com.github.k1mb1.vkr_backend.attendance.checkin.web.responses.CheckInRecordResponse;
import com.github.k1mb1.vkr_backend.attendance.checkin.web.responses.PublicCheckInSessionResponse;
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

class PublicCheckInControllerTest {

    CheckInSessionApi checkInSessionApi;
    CheckInRecordsApi checkInRecordsApi;
    RestTestClient client;

    @BeforeEach
    void setUp() {
        checkInSessionApi = mock(CheckInSessionApi.class);
        checkInRecordsApi = mock(CheckInRecordsApi.class);
        client = ControllerTestSupport.client(new PublicCheckInController(checkInSessionApi, checkInRecordsApi));
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private PublicCheckInSessionResponse publicResponse(UUID sessionId) {
        var now = Instant.now();
        var student = new PublicCheckInSessionResponse.Student(
            UUID.randomUUID(), "Иванов И. И.", null, null
        );
        return new PublicCheckInSessionResponse(
            sessionId,
            "Introduction to CS",
            Collections.<CheckInAudienceScope>emptyList(),
            CheckInSessionState.OPEN,
            now.plusSeconds(600),
            now.plusSeconds(900),
            now,
            List.of(student)
        );
    }

    // -----------------------------------------------------------------------
    // GET /api/check-in-sessions/public/{id} — 200
    // -----------------------------------------------------------------------

    @Test
    void getPublic_returnsOk() {
        var sessionId = UUID.randomUUID();
        var response = publicResponse(sessionId);
        when(checkInSessionApi.getPublic(sessionId)).thenReturn(response);

        client.get().uri("/api/check-in-sessions/public/{id}", sessionId)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").isEqualTo(sessionId.toString())
            .jsonPath("$.lessonTopic").isEqualTo("Introduction to CS")
            .jsonPath("$.state").isEqualTo("OPEN")
            .jsonPath("$.students").isArray()
            .jsonPath("$.students[0].username").isEqualTo("Иванов И. И.");
    }

    // -----------------------------------------------------------------------
    // GET /api/check-in-sessions/public/{id} — not found → 404
    // -----------------------------------------------------------------------

    @Test
    void getPublic_notFound_returns404() {
        var sessionId = UUID.randomUUID();
        when(checkInSessionApi.getPublic(sessionId))
            .thenThrow(new ResourceNotFoundException("CheckInSession", sessionId));

        client.get().uri("/api/check-in-sessions/public/{id}", sessionId)
            .exchange()
            .expectStatus().isNotFound();
    }

    // -----------------------------------------------------------------------
    // POST /api/check-in-sessions/public/{id}/check-in — 200
    // -----------------------------------------------------------------------

    @Test
    void checkIn_returnsOk() {
        var sessionId = UUID.randomUUID();
        var studentId = UUID.randomUUID();
        var recordId = UUID.randomUUID();
        var now = Instant.now();

        var recordResponse = new CheckInRecordResponse(recordId, sessionId, studentId, CheckInRecordStatus.PRESENT, now);
        when(checkInRecordsApi.checkIn(eq(sessionId), any())).thenReturn(recordResponse);

        var body = new StudentCheckInRequest(studentId);

        client.post().uri("/api/check-in-sessions/public/{id}/check-in", sessionId)
            .contentType(MediaType.APPLICATION_JSON)
            .body(body)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").isEqualTo(recordId.toString())
            .jsonPath("$.sessionId").isEqualTo(sessionId.toString())
            .jsonPath("$.studentId").isEqualTo(studentId.toString())
            .jsonPath("$.status").isEqualTo("PRESENT");
    }

    // -----------------------------------------------------------------------
    // POST /api/check-in-sessions/public/{id}/check-in — session not found → 404
    // -----------------------------------------------------------------------

    @Test
    void checkIn_sessionNotFound_returns404() {
        var sessionId = UUID.randomUUID();
        var studentId = UUID.randomUUID();
        when(checkInRecordsApi.checkIn(eq(sessionId), any()))
            .thenThrow(new ResourceNotFoundException("CheckInSession", sessionId));

        client.post().uri("/api/check-in-sessions/public/{id}/check-in", sessionId)
            .contentType(MediaType.APPLICATION_JSON)
            .body(new StudentCheckInRequest(studentId))
            .exchange()
            .expectStatus().isNotFound();
    }

    // -----------------------------------------------------------------------
    // POST /api/check-in-sessions/public/{id}/check-in — session closed → 400
    // -----------------------------------------------------------------------

    @Test
    void checkIn_sessionClosed_returns400() {
        var sessionId = UUID.randomUUID();
        var studentId = UUID.randomUUID();
        when(checkInRecordsApi.checkIn(eq(sessionId), any()))
            .thenThrow(new IllegalStateException("Check-in is closed for session"));

        client.post().uri("/api/check-in-sessions/public/{id}/check-in", sessionId)
            .contentType(MediaType.APPLICATION_JSON)
            .body(new StudentCheckInRequest(studentId))
            .exchange()
            .expectStatus().isBadRequest();
    }

    // -----------------------------------------------------------------------
    // POST /api/check-in-sessions/public/{id}/check-in — student not in audience → 400
    // -----------------------------------------------------------------------

    @Test
    void checkIn_studentNotInAudience_returns400() {
        var sessionId = UUID.randomUUID();
        var studentId = UUID.randomUUID();
        when(checkInRecordsApi.checkIn(eq(sessionId), any()))
            .thenThrow(new IllegalArgumentException("Student is not part of this lesson audience"));

        client.post().uri("/api/check-in-sessions/public/{id}/check-in", sessionId)
            .contentType(MediaType.APPLICATION_JSON)
            .body(new StudentCheckInRequest(studentId))
            .exchange()
            .expectStatus().isBadRequest();
    }
}
