package com.github.k1mb1.vkr_backend.subject.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.k1mb1.vkr_backend.common.error.ResourceNotFoundException;
import com.github.k1mb1.vkr_backend.subject.SubjectPenaltyPolicyApi;
import com.github.k1mb1.vkr_backend.subject.web.requests.PenaltyPolicyRequest;
import com.github.k1mb1.vkr_backend.subject.web.responses.PenaltyPolicyResponse;
import com.github.k1mb1.vkr_backend.support.ControllerTestSupport;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.client.RestTestClient;

class SubjectPenaltyPolicyControllerTest {

    SubjectPenaltyPolicyApi penaltyPolicyApi;
    RestTestClient client;

    @BeforeEach
    void setUp() {
        penaltyPolicyApi = mock(SubjectPenaltyPolicyApi.class);
        client = ControllerTestSupport.client(new SubjectPenaltyPolicyController(penaltyPolicyApi));
    }

    // ------------------------------------------------------------------ GET /{subjectId}

    @Test
    void getPenaltyPolicyReturnsOk() {
        var subjectId = UUID.randomUUID();
        var response = new PenaltyPolicyResponse(false, null, null, null, null, null,
            false, null, null, null, null, null);

        when(penaltyPolicyApi.getPenaltyPolicy(subjectId)).thenReturn(response);

        client.get().uri("/api/penalty-policy/subjects/{subjectId}", subjectId)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.enabled").isEqualTo(false)
            .jsonPath("$.bonusEnabled").isEqualTo(false);
    }

    @Test
    void getPenaltyPolicyReturns404WhenSubjectNotFound() {
        var subjectId = UUID.randomUUID();

        when(penaltyPolicyApi.getPenaltyPolicy(subjectId))
            .thenThrow(new ResourceNotFoundException("Subject", subjectId));

        client.get().uri("/api/penalty-policy/subjects/{subjectId}", subjectId)
            .exchange()
            .expectStatus().isNotFound();
    }

    // ------------------------------------------------------------------ PUT /{subjectId}

    @Test
    void updatePenaltyPolicyReturnsOk() {
        var subjectId = UUID.randomUUID();
        var request = new PenaltyPolicyRequest(false, null, null, null, null, null,
            false, null, null, null, null, null);
        var response = new PenaltyPolicyResponse(false, null, null, null, null, null,
            false, null, null, null, null, null);

        when(penaltyPolicyApi.updatePenaltyPolicy(eq(subjectId), any())).thenReturn(response);

        client.put().uri("/api/penalty-policy/subjects/{subjectId}", subjectId)
            .body(request)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.enabled").isEqualTo(false);
    }

    @Test
    void updatePenaltyPolicyReturns404WhenSubjectNotFound() {
        var subjectId = UUID.randomUUID();

        when(penaltyPolicyApi.updatePenaltyPolicy(eq(subjectId), any()))
            .thenThrow(new ResourceNotFoundException("Subject", subjectId));

        client.put().uri("/api/penalty-policy/subjects/{subjectId}", subjectId)
            .body(new PenaltyPolicyRequest(false, null, null, null, null, null,
                false, null, null, null, null, null))
            .exchange()
            .expectStatus().isNotFound();
    }

    @Test
    void updatePenaltyPolicyReturns400WhenIllegalArgument() {
        var subjectId = UUID.randomUUID();

        when(penaltyPolicyApi.updatePenaltyPolicy(eq(subjectId), any()))
            .thenThrow(new IllegalArgumentException("enabled=true requires all penalty fields"));

        client.put().uri("/api/penalty-policy/subjects/{subjectId}", subjectId)
            .body(new PenaltyPolicyRequest(false, null, null, null, null, null,
                false, null, null, null, null, null))
            .exchange()
            .expectStatus().isBadRequest();
    }
}
