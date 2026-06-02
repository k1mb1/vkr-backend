package com.github.k1mb1.vkr_backend.attendance.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.k1mb1.vkr_backend.attendance.AttendanceApi;
import com.github.k1mb1.vkr_backend.attendance.domain.AttendanceStatus;
import com.github.k1mb1.vkr_backend.attendance.web.requests.BulkUpsertAttendanceRequest;
import com.github.k1mb1.vkr_backend.attendance.web.requests.UpsertAttendanceRequest;
import com.github.k1mb1.vkr_backend.attendance.web.responses.AttendanceCellResponse;
import com.github.k1mb1.vkr_backend.attendance.web.responses.AttendanceTableResponse;
import com.github.k1mb1.vkr_backend.common.error.ResourceNotFoundException;
import com.github.k1mb1.vkr_backend.support.ControllerTestSupport;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;

class AttendanceControllerTest {

    AttendanceApi attendanceApi;
    RestTestClient client;

    @BeforeEach
    void setUp() {
        attendanceApi = mock(AttendanceApi.class);
        client = ControllerTestSupport.client(new AttendanceController(attendanceApi));
    }

    // -----------------------------------------------------------------------
    // GET /api/attendances?permissionId=...  — 200
    // -----------------------------------------------------------------------

    @Test
    void getAttendanceTable_returnsOk() {
        var permissionId = UUID.randomUUID();
        var table = new AttendanceTableResponse(List.of(), List.of(), List.of(), List.of());
        when(attendanceApi.getAttendanceTable(any())).thenReturn(table);

        client.get().uri("/api/attendances?permissionId=" + permissionId)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.audience").isArray()
            .jsonPath("$.students").isArray()
            .jsonPath("$.lessons").isArray()
            .jsonPath("$.attendances").isArray();
    }

    // -----------------------------------------------------------------------
    // GET /api/attendances?permissionId=... — permission not found → 404
    // -----------------------------------------------------------------------

    @Test
    void getAttendanceTable_permissionNotFound_returns404() {
        var permissionId = UUID.randomUUID();
        when(attendanceApi.getAttendanceTable(any()))
            .thenThrow(new ResourceNotFoundException("TeacherSubjectPermission", permissionId));

        client.get().uri("/api/attendances?permissionId=" + permissionId)
            .exchange()
            .expectStatus().isNotFound();
    }

    // -----------------------------------------------------------------------
    // GET /api/attendances?permissionId=... — illegal argument → 400
    // -----------------------------------------------------------------------

    @Test
    void getAttendanceTable_illegalArgument_returns400() {
        var permissionId = UUID.randomUUID();
        when(attendanceApi.getAttendanceTable(any()))
            .thenThrow(new IllegalArgumentException("Scope not visible"));

        client.get().uri("/api/attendances?permissionId=" + permissionId)
            .exchange()
            .expectStatus().isBadRequest();
    }

    // -----------------------------------------------------------------------
    // PUT /api/attendances — 200 with cells
    // -----------------------------------------------------------------------

    @Test
    void upsertAll_returnsOkWithCells() {
        var studentId = UUID.randomUUID();
        var scopeId = UUID.randomUUID();
        var cellId = UUID.randomUUID();

        var cell = new AttendanceCellResponse(cellId, studentId, scopeId, AttendanceStatus.PRESENT, null);
        when(attendanceApi.upsertAll(any())).thenReturn(List.of(cell));

        var item = new UpsertAttendanceRequest(studentId, scopeId, AttendanceStatus.PRESENT, null);
        var body = new BulkUpsertAttendanceRequest(List.of(item));

        client.put().uri("/api/attendances")
            .contentType(MediaType.APPLICATION_JSON)
            .body(body)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$[0].id").isEqualTo(cellId.toString())
            .jsonPath("$[0].studentId").isEqualTo(studentId.toString())
            .jsonPath("$[0].lessonScopeId").isEqualTo(scopeId.toString())
            .jsonPath("$[0].status").isEqualTo("PRESENT");
    }

    // -----------------------------------------------------------------------
    // PUT /api/attendances — duplicate pair → 400
    // -----------------------------------------------------------------------

    @Test
    void upsertAll_duplicatePair_returns400() {
        when(attendanceApi.upsertAll(any()))
            .thenThrow(new IllegalArgumentException("Duplicate (studentId, lessonScopeId)"));

        var studentId = UUID.randomUUID();
        var scopeId = UUID.randomUUID();
        var item = new UpsertAttendanceRequest(studentId, scopeId, AttendanceStatus.PRESENT, null);
        var body = new BulkUpsertAttendanceRequest(List.of(item));

        client.put().uri("/api/attendances")
            .contentType(MediaType.APPLICATION_JSON)
            .body(body)
            .exchange()
            .expectStatus().isBadRequest();
    }
}
