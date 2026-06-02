package com.github.k1mb1.vkr_backend.subject.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.k1mb1.vkr_backend.common.error.ResourceNotFoundException;
import com.github.k1mb1.vkr_backend.subject.SubjectAttendancePolicyApi;
import com.github.k1mb1.vkr_backend.subject.web.requests.AttendancePolicyRequest;
import com.github.k1mb1.vkr_backend.subject.web.responses.AttendancePolicyResponse;
import com.github.k1mb1.vkr_backend.support.ControllerTestSupport;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.client.RestTestClient;

class SubjectAttendancePolicyControllerTest {

    SubjectAttendancePolicyApi attendancePolicyApi;
    RestTestClient client;

    @BeforeEach
    void setUp() {
        attendancePolicyApi = mock(SubjectAttendancePolicyApi.class);
        client = ControllerTestSupport.client(new SubjectAttendancePolicyController(attendancePolicyApi));
    }

    // ------------------------------------------------------------------ GET /{subjectId}

    @Test
    void getAttendancePolicyReturnsOk() {
        var subjectId = UUID.randomUUID();
        var response = new AttendancePolicyResponse(false,
            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);

        when(attendancePolicyApi.getAttendancePolicy(subjectId)).thenReturn(response);

        client.get().uri("/api/attendance-policy/subjects/{subjectId}", subjectId)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.enabled").isEqualTo(false);
    }

    @Test
    void getAttendancePolicyReturns404WhenSubjectNotFound() {
        var subjectId = UUID.randomUUID();

        when(attendancePolicyApi.getAttendancePolicy(subjectId))
            .thenThrow(new ResourceNotFoundException("Subject", subjectId));

        client.get().uri("/api/attendance-policy/subjects/{subjectId}", subjectId)
            .exchange()
            .expectStatus().isNotFound();
    }

    // ------------------------------------------------------------------ PUT /{subjectId}

    @Test
    void updateAttendancePolicyReturnsOk() {
        var subjectId = UUID.randomUUID();
        var request = new AttendancePolicyRequest(true,
            new BigDecimal("1.0"), new BigDecimal("0.5"),
            new BigDecimal("-1.0"), new BigDecimal("0.0"));
        var response = new AttendancePolicyResponse(true,
            new BigDecimal("1.0"), new BigDecimal("0.5"),
            new BigDecimal("-1.0"), new BigDecimal("0.0"));

        when(attendancePolicyApi.updateAttendancePolicy(eq(subjectId), any())).thenReturn(response);

        client.put().uri("/api/attendance-policy/subjects/{subjectId}", subjectId)
            .body(request)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.enabled").isEqualTo(true);
    }

    @Test
    void updateAttendancePolicyReturns404WhenSubjectNotFound() {
        var subjectId = UUID.randomUUID();

        when(attendancePolicyApi.updateAttendancePolicy(eq(subjectId), any()))
            .thenThrow(new ResourceNotFoundException("Subject", subjectId));

        client.put().uri("/api/attendance-policy/subjects/{subjectId}", subjectId)
            .body(new AttendancePolicyRequest(false, null, null, null, null))
            .exchange()
            .expectStatus().isNotFound();
    }

    @Test
    void updateAttendancePolicyReturns400WhenValidationFails() {
        var subjectId = UUID.randomUUID();

        when(attendancePolicyApi.updateAttendancePolicy(eq(subjectId), any()))
            .thenThrow(new IllegalArgumentException("enabled=true requires all points fields"));

        client.put().uri("/api/attendance-policy/subjects/{subjectId}", subjectId)
            .body(new AttendancePolicyRequest(false, null, null, null, null))
            .exchange()
            .expectStatus().isBadRequest();
    }
}
