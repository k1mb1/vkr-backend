package com.github.k1mb1.vkr_backend.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.k1mb1.vkr_backend.common.exception.ConflictException;
import com.github.k1mb1.vkr_backend.teacher.controller.TeacherSubjectPermissionsController;
import com.github.k1mb1.vkr_backend.teacher.service.TeacherSubjectPermissionService;
import com.github.k1mb1.vkr_backend.teacher.service.dto.request.CreateTeacherSubjectPermissionRequest;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(TeacherSubjectPermissionsController.class)
class TeacherSubjectPermissionsControllerTest extends AbstractControllerTest {

    @MockitoBean
    TeacherSubjectPermissionService service;

    @Test
    void createReturns201AndDelegates() throws Exception {
        var request = new CreateTeacherSubjectPermissionRequest(UUID.randomUUID(), UUID.randomUUID(), true, null);
        mvc.perform(post("/api/teacher-subject-permissions")
                        .with(teacher())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(request)))
                .andExpect(status().isCreated());
        verify(service).create(any());
    }

    @Test
    void createRejectsNullTeacherId() throws Exception {
        var invalid = new CreateTeacherSubjectPermissionRequest(null, UUID.randomUUID(), true, null);
        mvc.perform(post("/api/teacher-subject-permissions")
                        .with(teacher())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors").isNotEmpty());
    }

    @Test
    void createRejectsScopedGrantWithoutScopes() throws Exception {
        // allPermissions=false requires a non-empty scopes list (@AssertTrue)
        var invalid = new CreateTeacherSubjectPermissionRequest(UUID.randomUUID(), UUID.randomUUID(), false, List.of());
        mvc.perform(post("/api/teacher-subject-permissions")
                        .with(teacher())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }

    @Test
    void createMapsConflictTo409() throws Exception {
        when(service.create(any())).thenThrow(new ConflictException("Permission already exists"));
        var request = new CreateTeacherSubjectPermissionRequest(UUID.randomUUID(), UUID.randomUUID(), true, null);
        mvc.perform(post("/api/teacher-subject-permissions")
                        .with(teacher())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"));
    }
}
