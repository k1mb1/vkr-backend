package com.github.k1mb1.vkr_backend.grading.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.k1mb1.vkr_backend.common.error.ResourceNotFoundException;
import com.github.k1mb1.vkr_backend.grading.GradingApi;
import com.github.k1mb1.vkr_backend.grading.web.requests.CreateAssignmentsRequest;
import com.github.k1mb1.vkr_backend.grading.web.responses.AssignmentResponse;
import com.github.k1mb1.vkr_backend.support.ControllerTestSupport;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.client.RestTestClient;

class AssignmentControllerTest {

    GradingApi gradingApi;
    RestTestClient client;

    @BeforeEach
    void setUp() {
        gradingApi = mock(GradingApi.class);
        client = ControllerTestSupport.client(new AssignmentController(gradingApi));
    }

    // =========================================================================
    // GET /api/assignments?lessonId=...
    // =========================================================================

    @Test
    void getByLesson_returnsOkWithList() {
        var lessonId = UUID.randomUUID();
        var assignmentId1 = UUID.randomUUID();
        var assignmentId2 = UUID.randomUUID();

        when(gradingApi.getAssignmentsByLesson(lessonId)).thenReturn(List.of(
            new AssignmentResponse(assignmentId1, lessonId, 1, 10, true),
            new AssignmentResponse(assignmentId2, lessonId, 2, 20, false)
        ));

        client.get().uri("/api/assignments?lessonId=" + lessonId)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$[0].id").isEqualTo(assignmentId1.toString())
            .jsonPath("$[0].lessonId").isEqualTo(lessonId.toString())
            .jsonPath("$[0].order").isEqualTo(1)
            .jsonPath("$[0].maxPoints").isEqualTo(10)
            .jsonPath("$[0].required").isEqualTo(true)
            .jsonPath("$[1].id").isEqualTo(assignmentId2.toString())
            .jsonPath("$[1].order").isEqualTo(2);
    }

    @Test
    void getByLesson_emptyList_returnsOk() {
        var lessonId = UUID.randomUUID();
        when(gradingApi.getAssignmentsByLesson(lessonId)).thenReturn(List.of());

        client.get().uri("/api/assignments?lessonId=" + lessonId)
            .exchange()
            .expectStatus().isOk()
            .expectBody().json("[]");
    }

    // =========================================================================
    // POST /api/assignments
    // =========================================================================

    @Test
    void create_returnsOkWithAssignments() {
        var lessonId = UUID.randomUUID();
        var assignmentId = UUID.randomUUID();
        var request = new CreateAssignmentsRequest(lessonId, List.of(
            new CreateAssignmentsRequest.Item(10, true)
        ));

        when(gradingApi.createAssignments(any(CreateAssignmentsRequest.class))).thenReturn(List.of(
            new AssignmentResponse(assignmentId, lessonId, 1, 10, true)
        ));

        client.post().uri("/api/assignments")
            .body(request)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$[0].id").isEqualTo(assignmentId.toString())
            .jsonPath("$[0].lessonId").isEqualTo(lessonId.toString())
            .jsonPath("$[0].order").isEqualTo(1)
            .jsonPath("$[0].maxPoints").isEqualTo(10)
            .jsonPath("$[0].required").isEqualTo(true);
    }

    @Test
    void create_alreadyHasAssignments_returns400() {
        var lessonId = UUID.randomUUID();
        var request = new CreateAssignmentsRequest(lessonId, List.of(
            new CreateAssignmentsRequest.Item(10, true)
        ));

        when(gradingApi.createAssignments(any(CreateAssignmentsRequest.class)))
            .thenThrow(new IllegalStateException("Lesson " + lessonId + " already has assignments."));

        client.post().uri("/api/assignments")
            .body(request)
            .exchange()
            .expectStatus().isBadRequest();
    }

    @Test
    void create_multipleItems_returnsAllInOrder() {
        var lessonId = UUID.randomUUID();
        var id1 = UUID.randomUUID();
        var id2 = UUID.randomUUID();
        var request = new CreateAssignmentsRequest(lessonId, List.of(
            new CreateAssignmentsRequest.Item(10, true),
            new CreateAssignmentsRequest.Item(20, false)
        ));

        when(gradingApi.createAssignments(any(CreateAssignmentsRequest.class))).thenReturn(List.of(
            new AssignmentResponse(id1, lessonId, 1, 10, true),
            new AssignmentResponse(id2, lessonId, 2, 20, false)
        ));

        client.post().uri("/api/assignments")
            .body(request)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$[0].order").isEqualTo(1)
            .jsonPath("$[1].order").isEqualTo(2);
    }

    // =========================================================================
    // DELETE /api/assignments/{id}
    // =========================================================================

    @Test
    void delete_found_returns204() {
        var id = UUID.randomUUID();
        doNothing().when(gradingApi).deleteAssignment(id);

        client.delete().uri("/api/assignments/{id}", id)
            .exchange()
            .expectStatus().isNoContent();
    }

    @Test
    void delete_notFound_returns404() {
        var id = UUID.randomUUID();
        doThrow(new ResourceNotFoundException("Assignment", id))
            .when(gradingApi).deleteAssignment(id);

        client.delete().uri("/api/assignments/{id}", id)
            .exchange()
            .expectStatus().isNotFound();
    }

    // =========================================================================
    // DELETE /api/assignments/lessons/{lessonId}
    // =========================================================================

    @Test
    void deleteAllOfLesson_returns204() {
        var lessonId = UUID.randomUUID();
        doNothing().when(gradingApi).deleteAssignmentsOfLesson(lessonId);

        client.delete().uri("/api/assignments/lessons/{lessonId}", lessonId)
            .exchange()
            .expectStatus().isNoContent();
    }

    @Test
    void deleteAllOfLesson_notFound_noExceptionFromEndpoint_returns204() {
        // deleteAssignmentsOfLesson is a best-effort delete — it does not throw if no rows exist
        var lessonId = UUID.randomUUID();
        doNothing().when(gradingApi).deleteAssignmentsOfLesson(lessonId);

        client.delete().uri("/api/assignments/lessons/{lessonId}", lessonId)
            .exchange()
            .expectStatus().isNoContent();
    }
}
