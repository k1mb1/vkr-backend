package com.github.k1mb1.vkr_backend.lesson.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.k1mb1.vkr_backend.common.error.ResourceNotFoundException;
import com.github.k1mb1.vkr_backend.lesson.LessonScopesApi;
import com.github.k1mb1.vkr_backend.lesson.web.requests.BulkAddLessonScopesRequest;
import com.github.k1mb1.vkr_backend.lesson.web.responses.LessonScopeResponse;
import com.github.k1mb1.vkr_backend.support.ControllerTestSupport;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.client.RestTestClient;

class LessonScopesControllerTest {

    LessonScopesApi lessonScopesApi;
    RestTestClient client;

    @BeforeEach
    void setUp() {
        lessonScopesApi = mock(LessonScopesApi.class);
        client = ControllerTestSupport.client(new LessonScopesController(lessonScopesApi));
    }

    // -------------------------------------------------------------------------
    // POST /api/lessons/{lessonId}/scopes — success → 201
    // -------------------------------------------------------------------------

    @Test
    void addScopes_returns201WithCreatedScopes() {
        var lessonId = UUID.randomUUID();
        var scopeId = UUID.randomUUID();
        var groupId = UUID.randomUUID();
        var startedAt = LocalDate.of(2025, 9, 1);

        var scopeResponse = new LessonScopeResponse(
            scopeId, groupId, "Group A", null, null, startedAt, false
        );

        when(lessonScopesApi.addScopes(eq(lessonId), any(BulkAddLessonScopesRequest.class)))
            .thenReturn(List.of(scopeResponse));

        client.post().uri("/api/lessons/{lessonId}/scopes", lessonId)
            .body(new BulkAddLessonScopesRequest(List.of(
                new BulkAddLessonScopesRequest.Item(null, startedAt)
            )))
            .exchange()
            .expectStatus().isCreated()
            .expectBody()
            .jsonPath("$[0].id").isEqualTo(scopeId.toString())
            .jsonPath("$[0].groupId").isEqualTo(groupId.toString())
            .jsonPath("$[0].startedAt").isEqualTo(startedAt.toString())
            .jsonPath("$[0].allGroups").isEqualTo(false);
    }

    @Test
    void addScopes_allGroupsScope_returns201() {
        var lessonId = UUID.randomUUID();
        var scopeId = UUID.randomUUID();
        var startedAt = LocalDate.of(2025, 10, 15);

        var scopeResponse = new LessonScopeResponse(
            scopeId, null, null, null, null, startedAt, true
        );

        when(lessonScopesApi.addScopes(eq(lessonId), any(BulkAddLessonScopesRequest.class)))
            .thenReturn(List.of(scopeResponse));

        client.post().uri("/api/lessons/{lessonId}/scopes", lessonId)
            .body(new BulkAddLessonScopesRequest(List.of(
                new BulkAddLessonScopesRequest.Item(null, startedAt)
            )))
            .exchange()
            .expectStatus().isCreated()
            .expectBody()
            .jsonPath("$[0].id").isEqualTo(scopeId.toString())
            .jsonPath("$[0].allGroups").isEqualTo(true)
            .jsonPath("$[0].startedAt").isEqualTo(startedAt.toString());
    }

    // -------------------------------------------------------------------------
    // POST — lesson not found → 404
    // -------------------------------------------------------------------------

    @Test
    void addScopes_lessonNotFound_returns404() {
        var lessonId = UUID.randomUUID();
        when(lessonScopesApi.addScopes(eq(lessonId), any(BulkAddLessonScopesRequest.class)))
            .thenThrow(new ResourceNotFoundException("Lesson", lessonId));

        client.post().uri("/api/lessons/{lessonId}/scopes", lessonId)
            .body(new BulkAddLessonScopesRequest(List.of(
                new BulkAddLessonScopesRequest.Item(null, LocalDate.of(2025, 11, 1))
            )))
            .exchange()
            .expectStatus().isNotFound();
    }

    // -------------------------------------------------------------------------
    // POST — overlap validation → 400
    // -------------------------------------------------------------------------

    @Test
    void addScopes_overlap_returns400() {
        var lessonId = UUID.randomUUID();
        when(lessonScopesApi.addScopes(eq(lessonId), any(BulkAddLessonScopesRequest.class)))
            .thenThrow(new IllegalArgumentException("Scope audience overlap within lesson"));

        client.post().uri("/api/lessons/{lessonId}/scopes", lessonId)
            .body(new BulkAddLessonScopesRequest(List.of(
                new BulkAddLessonScopesRequest.Item(null, LocalDate.of(2025, 12, 1)),
                new BulkAddLessonScopesRequest.Item(null, LocalDate.of(2025, 12, 8))
            )))
            .exchange()
            .expectStatus().isBadRequest();
    }

    // -------------------------------------------------------------------------
    // POST — multiple scopes created → list has correct size
    // -------------------------------------------------------------------------

    @Test
    void addScopes_multipleScopes_returnsAllInResponse() {
        var lessonId = UUID.randomUUID();
        var date1 = LocalDate.of(2026, 1, 10);
        var date2 = LocalDate.of(2026, 1, 17);
        var groupId1 = UUID.randomUUID();
        var groupId2 = UUID.randomUUID();

        var scope1 = new LessonScopeResponse(UUID.randomUUID(), groupId1, "G1", null, null, date1, false);
        var scope2 = new LessonScopeResponse(UUID.randomUUID(), groupId2, "G2", null, null, date2, false);

        when(lessonScopesApi.addScopes(eq(lessonId), any(BulkAddLessonScopesRequest.class)))
            .thenReturn(List.of(scope1, scope2));

        client.post().uri("/api/lessons/{lessonId}/scopes", lessonId)
            .body(new BulkAddLessonScopesRequest(List.of(
                new BulkAddLessonScopesRequest.Item(null, date1),
                new BulkAddLessonScopesRequest.Item(null, date2)
            )))
            .exchange()
            .expectStatus().isCreated()
            .expectBody()
            .jsonPath("$[0].groupId").isEqualTo(groupId1.toString())
            .jsonPath("$[1].groupId").isEqualTo(groupId2.toString());
    }
}
