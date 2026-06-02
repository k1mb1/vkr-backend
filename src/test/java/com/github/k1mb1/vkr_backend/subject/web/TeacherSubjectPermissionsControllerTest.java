package com.github.k1mb1.vkr_backend.subject.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.k1mb1.vkr_backend.common.error.ResourceNotFoundException;
import com.github.k1mb1.vkr_backend.subject.TeacherSubjectPermissionsApi;
import com.github.k1mb1.vkr_backend.subject.web.requests.CreateTeacherSubjectPermissionRequest;
import com.github.k1mb1.vkr_backend.subject.web.requests.UpdateTeacherSubjectPermissionRequest;
import com.github.k1mb1.vkr_backend.subject.web.responses.TeacherSubjectPermissionResponse;
import com.github.k1mb1.vkr_backend.support.ControllerTestSupport;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.client.RestTestClient;

class TeacherSubjectPermissionsControllerTest {

    TeacherSubjectPermissionsApi teacherSubjectPermissionsApi;
    RestTestClient client;

    @BeforeEach
    void setUp() {
        teacherSubjectPermissionsApi = mock(TeacherSubjectPermissionsApi.class);
        client = ControllerTestSupport.client(
            new TeacherSubjectPermissionsController(teacherSubjectPermissionsApi));
    }

    // ------------------------------------------------------------------ GET /?subjectId=...

    @Test
    void getPermissionsBySubjectReturnsOk() {
        var subjectId = UUID.randomUUID();
        var permissionId = UUID.randomUUID();
        var teacherId = UUID.randomUUID();
        var response = buildResponse(permissionId, teacherId, subjectId);

        when(teacherSubjectPermissionsApi.getPermissionsBySubject(subjectId))
            .thenReturn(List.of(response));

        client.get().uri("/api/teacher-subject-permissions?subjectId=" + subjectId)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$[0].id").isEqualTo(permissionId.toString())
            .jsonPath("$[0].subjectId").isEqualTo(subjectId.toString());
    }

    // ------------------------------------------------------------------ GET /single

    @Test
    void getPermissionReturnsOk() {
        var subjectId = UUID.randomUUID();
        var teacherId = UUID.randomUUID();
        var permissionId = UUID.randomUUID();
        var response = buildResponse(permissionId, teacherId, subjectId);

        when(teacherSubjectPermissionsApi.getPermission(subjectId, teacherId)).thenReturn(response);

        client.get().uri("/api/teacher-subject-permissions/single?subjectId=" + subjectId
                + "&teacherId=" + teacherId)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").isEqualTo(permissionId.toString())
            .jsonPath("$.teacherId").isEqualTo(teacherId.toString());
    }

    @Test
    void getPermissionReturns404WhenNotFound() {
        var subjectId = UUID.randomUUID();
        var teacherId = UUID.randomUUID();

        when(teacherSubjectPermissionsApi.getPermission(subjectId, teacherId))
            .thenThrow(new EntityNotFoundException("TeacherSubjectPermission not found"));

        client.get().uri("/api/teacher-subject-permissions/single?subjectId=" + subjectId
                + "&teacherId=" + teacherId)
            .exchange()
            .expectStatus().isNotFound();
    }

    // ------------------------------------------------------------------ POST /

    @Test
    void createReturnsCreated() {
        var subjectId = UUID.randomUUID();
        var teacherId = UUID.randomUUID();
        var permissionId = UUID.randomUUID();
        var response = buildResponse(permissionId, teacherId, subjectId);

        when(teacherSubjectPermissionsApi.create(any())).thenReturn(response);

        client.post().uri("/api/teacher-subject-permissions")
            .body(new CreateTeacherSubjectPermissionRequest(teacherId, subjectId, true, null))
            .exchange()
            .expectStatus().isCreated()
            .expectBody()
            .jsonPath("$.id").isEqualTo(permissionId.toString());
    }

    @Test
    void createReturns400WhenDuplicatePermission() {
        when(teacherSubjectPermissionsApi.create(any()))
            .thenThrow(new IllegalStateException("Permission already exists"));

        var request = new CreateTeacherSubjectPermissionRequest(
            UUID.randomUUID(), UUID.randomUUID(), true, null);

        client.post().uri("/api/teacher-subject-permissions")
            .body(request)
            .exchange()
            .expectStatus().isBadRequest();
    }

    // ------------------------------------------------------------------ PATCH /{id}

    @Test
    void updateReturnsOk() {
        var permissionId = UUID.randomUUID();
        var subjectId = UUID.randomUUID();
        var teacherId = UUID.randomUUID();
        var response = buildResponse(permissionId, teacherId, subjectId);

        when(teacherSubjectPermissionsApi.update(eq(permissionId), any())).thenReturn(response);

        client.patch().uri("/api/teacher-subject-permissions/{id}", permissionId)
            .body(new UpdateTeacherSubjectPermissionRequest(null, null, null))
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").isEqualTo(permissionId.toString());
    }

    @Test
    void updateReturns404WhenNotFound() {
        var permissionId = UUID.randomUUID();

        when(teacherSubjectPermissionsApi.update(eq(permissionId), any()))
            .thenThrow(new ResourceNotFoundException("TeacherSubjectPermission", permissionId));

        client.patch().uri("/api/teacher-subject-permissions/{id}", permissionId)
            .body(new UpdateTeacherSubjectPermissionRequest(null, null, null))
            .exchange()
            .expectStatus().isNotFound();
    }

    // ------------------------------------------------------------------ DELETE /{id}

    @Test
    void deleteReturnsNoContent() {
        var permissionId = UUID.randomUUID();

        doNothing().when(teacherSubjectPermissionsApi).delete(permissionId);

        client.delete().uri("/api/teacher-subject-permissions/{id}", permissionId)
            .exchange()
            .expectStatus().isNoContent();
    }

    @Test
    void deleteReturns404WhenNotFound() {
        var permissionId = UUID.randomUUID();

        doThrow(new ResourceNotFoundException("TeacherSubjectPermission", permissionId))
            .when(teacherSubjectPermissionsApi).delete(permissionId);

        client.delete().uri("/api/teacher-subject-permissions/{id}", permissionId)
            .exchange()
            .expectStatus().isNotFound();
    }

    // ------------------------------------------------------------------ helpers

    private TeacherSubjectPermissionResponse buildResponse(UUID id, UUID teacherId, UUID subjectId) {
        return new TeacherSubjectPermissionResponse(id, teacherId, "Teacher Name",
            subjectId, true, List.of(), null, null);
    }
}
