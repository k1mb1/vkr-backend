package com.github.k1mb1.vkr_backend.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.k1mb1.vkr_backend.subject.controller.SubjectsController;
import com.github.k1mb1.vkr_backend.subject.service.SubjectService;
import com.github.k1mb1.vkr_backend.subject.service.dto.request.CreateSubjectRequest;
import com.github.k1mb1.vkr_backend.subject.service.dto.request.UpdateSubjectRequest;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(SubjectsController.class)
class SubjectsControllerTest extends AbstractControllerTest {

    @MockitoBean
    SubjectService subjectService;

    @Test
    void createSubjectRequiresAuthentication() throws Exception {
        mvc.perform(post("/api/subjects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(validCreate())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createSubjectReturns201AndDelegates() throws Exception {
        mvc.perform(post("/api/subjects")
                        .with(teacher())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(validCreate())))
                .andExpect(status().isCreated());
        verify(subjectService).createSubject(any());
    }

    @Test
    void createSubjectRejectsBlankNameWithValidationError() throws Exception {
        var invalid = new CreateSubjectRequest("  ", "d", List.of(UUID.randomUUID()), UUID.randomUUID());
        mvc.perform(post("/api/subjects")
                        .with(teacher())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors").isNotEmpty());
    }

    @Test
    void createSubjectRejectsMalformedBody() throws Exception {
        mvc.perform(post("/api/subjects")
                        .with(teacher())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ not json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MALFORMED_BODY"));
    }

    @Test
    void updateSubjectMapsEntityNotFoundTo404() throws Exception {
        when(subjectService.updateSubject(any(), any())).thenThrow(new EntityNotFoundException("Subject not found"));
        var body = json.writeValueAsString(
                UpdateSubjectRequest.builder().name("Math").build());
        mvc.perform(patch("/api/subjects/{id}", UUID.randomUUID())
                        .with(teacher())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void getSubjectsPageReturns200WithTeacherFilter() throws Exception {
        mvc.perform(get("/api/subjects")
                        .with(teacher())
                        .param("teacherId", UUID.randomUUID().toString()))
                .andExpect(status().isOk());
    }

    @Test
    void getSubjectsPageRejectsMissingTeacherId() throws Exception {
        mvc.perform(get("/api/subjects").with(teacher()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }

    private static CreateSubjectRequest validCreate() {
        return new CreateSubjectRequest("Math", "desc", List.of(UUID.randomUUID()), UUID.randomUUID());
    }
}
