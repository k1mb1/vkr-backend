package com.github.k1mb1.vkr_backend.subject.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.k1mb1.vkr_backend.common.error.ResourceNotFoundException;
import com.github.k1mb1.vkr_backend.subject.SubjectsApi;
import com.github.k1mb1.vkr_backend.subject.web.requests.CreateSubjectRequest;
import com.github.k1mb1.vkr_backend.subject.web.requests.UpdateSubjectRequest;
import com.github.k1mb1.vkr_backend.subject.web.responses.SubjectPageResponse;
import com.github.k1mb1.vkr_backend.subject.web.responses.SubjectResponse;
import com.github.k1mb1.vkr_backend.support.ControllerTestSupport;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.client.RestTestClient;

class SubjectsControllerTest {

    SubjectsApi subjectsApi;
    RestTestClient client;

    @BeforeEach
    void setUp() {
        subjectsApi = mock(SubjectsApi.class);
        client = ControllerTestSupport.client(new SubjectsController(subjectsApi));
    }

    // ------------------------------------------------------------------ PATCH /{id}

    @Test
    void updateSubjectReturnsOk() {
        var id = UUID.randomUUID();
        var response = SubjectResponse.builder().id(id).name("Updated Math").build();

        when(subjectsApi.updateSubject(eq(id), any())).thenReturn(response);

        client.patch().uri("/api/subjects/{id}", id)
            .body(new UpdateSubjectRequest("Updated Math", null, null))
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").isEqualTo(id.toString())
            .jsonPath("$.name").isEqualTo("Updated Math");
    }

    @Test
    void updateSubjectReturns404WhenNotFound() {
        var id = UUID.randomUUID();

        when(subjectsApi.updateSubject(eq(id), any()))
            .thenThrow(new EntityNotFoundException("Subject not found: " + id));

        client.patch().uri("/api/subjects/{id}", id)
            .body(new UpdateSubjectRequest("Name", null, null))
            .exchange()
            .expectStatus().isNotFound();
    }

    // ------------------------------------------------------------------ POST /

    @Test
    void createSubjectReturnsCreated() {
        var id = UUID.randomUUID();
        var teacherId = UUID.randomUUID();
        var groupId = UUID.randomUUID();
        var response = SubjectResponse.builder().id(id).name("Math").build();

        when(subjectsApi.createSubject(any())).thenReturn(response);

        client.post().uri("/api/subjects")
            .body(new CreateSubjectRequest("Math", "Description", List.of(groupId), teacherId))
            .exchange()
            .expectStatus().isCreated()
            .expectBody()
            .jsonPath("$.id").isEqualTo(id.toString())
            .jsonPath("$.name").isEqualTo("Math");
    }

    // ------------------------------------------------------------------ GET /

    @Test
    void getPageReturnsOk() {
        var teacherId = UUID.randomUUID();
        var subjectId = UUID.randomUUID();
        var pageItem = SubjectPageResponse.builder().id(subjectId).name("Math").build();

        when(subjectsApi.getPage(any(), any()))
            .thenReturn(new PageImpl<>(List.of(pageItem), PageRequest.of(0, 20), 1));

        client.get().uri("/api/subjects?teacherId=" + teacherId)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.content[0].id").isEqualTo(subjectId.toString())
            .jsonPath("$.content[0].name").isEqualTo("Math");
    }
}
