package com.github.k1mb1.vkr_backend.grading.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.k1mb1.vkr_backend.common.error.ResourceNotFoundException;
import com.github.k1mb1.vkr_backend.grading.GradingApi;
import com.github.k1mb1.vkr_backend.grading.web.filters.GradingFilter;
import com.github.k1mb1.vkr_backend.grading.web.requests.BulkUpsertGradesRequest;
import com.github.k1mb1.vkr_backend.grading.web.requests.UpsertGradeRequest;
import com.github.k1mb1.vkr_backend.grading.web.responses.AssignmentResponse;
import com.github.k1mb1.vkr_backend.grading.web.responses.GradeCellResponse;
import com.github.k1mb1.vkr_backend.grading.web.responses.GradingTableResponse;
import com.github.k1mb1.vkr_backend.support.ControllerTestSupport;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.client.RestTestClient;

class GradingControllerTest {

    GradingApi gradingApi;
    RestTestClient client;

    @BeforeEach
    void setUp() {
        gradingApi = mock(GradingApi.class);
        client = ControllerTestSupport.client(new GradingController(gradingApi));
    }

    // =========================================================================
    // GET /api/grades — getGradingTable
    // =========================================================================

    @Test
    void getGradingTable_returnsOk() {
        var permissionId = UUID.randomUUID();
        var tableResponse = new GradingTableResponse(
            null, null, List.of(), List.of(), List.of(), List.of(), List.of(), List.of()
        );

        when(gradingApi.getGradingTable(any(GradingFilter.class))).thenReturn(tableResponse);

        client.get().uri("/api/grades?permissionId=" + permissionId)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.students").isArray()
            .jsonPath("$.assignments").isArray()
            .jsonPath("$.grades").isArray();
    }

    @Test
    void getGradingTable_permissionNotFound_returns404() {
        var permissionId = UUID.randomUUID();

        when(gradingApi.getGradingTable(any(GradingFilter.class)))
            .thenThrow(new ResourceNotFoundException("TeacherSubjectPermission", permissionId));

        client.get().uri("/api/grades?permissionId=" + permissionId)
            .exchange()
            .expectStatus().isNotFound();
    }

    @Test
    void getGradingTable_illegalArgument_returns400() {
        var permissionId = UUID.randomUUID();

        when(gradingApi.getGradingTable(any(GradingFilter.class)))
            .thenThrow(new IllegalArgumentException("lesson mismatch"));

        client.get().uri("/api/grades?permissionId=" + permissionId)
            .exchange()
            .expectStatus().isBadRequest();
    }

    @Test
    void getGradingTable_withLessonIdFilter_returnsOk() {
        var permissionId = UUID.randomUUID();
        var lessonId = UUID.randomUUID();
        var tableResponse = new GradingTableResponse(
            null, null, List.of(), List.of(), List.of(), List.of(), List.of(), List.of()
        );

        when(gradingApi.getGradingTable(any(GradingFilter.class))).thenReturn(tableResponse);

        client.get().uri("/api/grades?permissionId=" + permissionId + "&lessonId=" + lessonId)
            .exchange()
            .expectStatus().isOk();
    }

    // =========================================================================
    // PUT /api/grades — upsertGrades
    // =========================================================================

    @Test
    void upsertGrades_returnsOkWithCells() {
        var studentId = UUID.randomUUID();
        var lessonId = UUID.randomUUID();
        var assignmentId = UUID.randomUUID();
        var cellId = UUID.randomUUID();

        var request = new BulkUpsertGradesRequest(List.of(
            new UpsertGradeRequest(studentId, lessonId, assignmentId, 8, "comment")
        ));
        var cell = new GradeCellResponse(cellId, studentId, lessonId, assignmentId, null, null, 8, "comment");

        when(gradingApi.upsertGrades(any(BulkUpsertGradesRequest.class))).thenReturn(List.of(cell));

        client.put().uri("/api/grades")
            .body(request)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$[0].id").isEqualTo(cellId.toString())
            .jsonPath("$[0].studentId").isEqualTo(studentId.toString())
            .jsonPath("$[0].lessonId").isEqualTo(lessonId.toString())
            .jsonPath("$[0].assignmentId").isEqualTo(assignmentId.toString())
            .jsonPath("$[0].score").isEqualTo(8)
            .jsonPath("$[0].comment").isEqualTo("comment");
    }

    @Test
    void upsertGrades_duplicateKey_returns400() {
        var studentId = UUID.randomUUID();
        var lessonId = UUID.randomUUID();
        var assignmentId = UUID.randomUUID();

        var request = new BulkUpsertGradesRequest(List.of(
            new UpsertGradeRequest(studentId, lessonId, assignmentId, 5, null),
            new UpsertGradeRequest(studentId, lessonId, assignmentId, 3, null)
        ));

        when(gradingApi.upsertGrades(any(BulkUpsertGradesRequest.class)))
            .thenThrow(new IllegalArgumentException("Duplicate (studentId, lessonId, assignmentId)"));

        client.put().uri("/api/grades")
            .body(request)
            .exchange()
            .expectStatus().isBadRequest();
    }

    @Test
    void upsertGrades_assignmentNotFound_returns404() {
        var studentId = UUID.randomUUID();
        var lessonId = UUID.randomUUID();
        var assignmentId = UUID.randomUUID();

        var request = new BulkUpsertGradesRequest(List.of(
            new UpsertGradeRequest(studentId, lessonId, assignmentId, 5, null)
        ));

        when(gradingApi.upsertGrades(any(BulkUpsertGradesRequest.class)))
            .thenThrow(new ResourceNotFoundException("Assignment", assignmentId));

        client.put().uri("/api/grades")
            .body(request)
            .exchange()
            .expectStatus().isNotFound();
    }

    @Test
    void upsertGrades_scoreExceedsMax_returns400() {
        var studentId = UUID.randomUUID();
        var lessonId = UUID.randomUUID();
        var assignmentId = UUID.randomUUID();

        var request = new BulkUpsertGradesRequest(List.of(
            new UpsertGradeRequest(studentId, lessonId, assignmentId, 100, null)
        ));

        when(gradingApi.upsertGrades(any(BulkUpsertGradesRequest.class)))
            .thenThrow(new IllegalArgumentException("Score 100 exceeds assignment maxPoints"));

        client.put().uri("/api/grades")
            .body(request)
            .exchange()
            .expectStatus().isBadRequest();
    }
}
