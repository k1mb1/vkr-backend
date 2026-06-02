package com.github.k1mb1.vkr_backend.lesson.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.k1mb1.vkr_backend.common.error.ResourceNotFoundException;
import com.github.k1mb1.vkr_backend.lesson.LessonApi;
import com.github.k1mb1.vkr_backend.lesson.domain.LessonType;
import com.github.k1mb1.vkr_backend.lesson.web.filters.LessonFilter;
import com.github.k1mb1.vkr_backend.lesson.web.requests.BulkCreateLessonsRequest;
import com.github.k1mb1.vkr_backend.lesson.web.requests.SetActiveLessonRequest;
import com.github.k1mb1.vkr_backend.lesson.web.requests.UpdateLessonRequest;
import com.github.k1mb1.vkr_backend.lesson.web.responses.LessonResponse;
import com.github.k1mb1.vkr_backend.support.ControllerTestSupport;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.client.RestTestClient;

class LessonControllerTest {

    LessonApi lessonApi;
    RestTestClient client;

    @BeforeEach
    void setUp() {
        lessonApi = mock(LessonApi.class);
        client = ControllerTestSupport.client(new LessonController(lessonApi));
    }

    // -------------------------------------------------------------------------
    // GET /api/lessons  — list
    // -------------------------------------------------------------------------

    @Test
    void getLessons_returnsOkWithList() {
        var permissionId = UUID.randomUUID();
        var lessonId = UUID.randomUUID();
        var subjectId = UUID.randomUUID();

        when(lessonApi.getLessons(any(LessonFilter.class)))
            .thenReturn(List.of(lessonResponse(lessonId, subjectId)));

        client.get().uri("/api/lessons?permissionId={pid}", permissionId)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$[0].id").isEqualTo(lessonId.toString())
            .jsonPath("$[0].subjectId").isEqualTo(subjectId.toString())
            .jsonPath("$[0].type").isEqualTo("LECTURE");
    }

    @Test
    void getLessons_emptyList_returnsOk() {
        when(lessonApi.getLessons(any(LessonFilter.class))).thenReturn(List.of());

        client.get().uri("/api/lessons?permissionId={pid}", UUID.randomUUID())
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .json("[]");
    }

    // -------------------------------------------------------------------------
    // GET /api/lessons/{id}
    // -------------------------------------------------------------------------

    @Test
    void getLessonById_found_returnsOk() {
        var id = UUID.randomUUID();
        var subjectId = UUID.randomUUID();

        when(lessonApi.getLessonById(id)).thenReturn(lessonResponse(id, subjectId));

        client.get().uri("/api/lessons/{id}", id)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").isEqualTo(id.toString())
            .jsonPath("$.topic").isEqualTo("Topic")
            .jsonPath("$.active").isEqualTo(false);
    }

    @Test
    void getLessonById_notFound_returns404() {
        var id = UUID.randomUUID();
        when(lessonApi.getLessonById(id))
            .thenThrow(new ResourceNotFoundException("Lesson", id));

        client.get().uri("/api/lessons/{id}", id)
            .exchange()
            .expectStatus().isNotFound();
    }

    // -------------------------------------------------------------------------
    // PUT /api/lessons/{id}
    // -------------------------------------------------------------------------

    @Test
    void updateLesson_returnsOk() {
        var id = UUID.randomUUID();
        var subjectId = UUID.randomUUID();
        var request = new UpdateLessonRequest(null, null, null);

        when(lessonApi.updateLesson(eq(id), any(UpdateLessonRequest.class)))
            .thenReturn(lessonResponse(id, subjectId));

        client.put().uri("/api/lessons/{id}", id)
            .body(request)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").isEqualTo(id.toString());
    }

    @Test
    void updateLesson_notFound_returns404() {
        var id = UUID.randomUUID();
        when(lessonApi.updateLesson(eq(id), any(UpdateLessonRequest.class)))
            .thenThrow(new ResourceNotFoundException("Lesson", id));

        client.put().uri("/api/lessons/{id}", id)
            .body(new UpdateLessonRequest(null, null, null))
            .exchange()
            .expectStatus().isNotFound();
    }

    // -------------------------------------------------------------------------
    // PATCH /api/lessons/{id}/active
    // -------------------------------------------------------------------------

    @Test
    void setActive_true_returnsOk() {
        var id = UUID.randomUUID();
        var subjectId = UUID.randomUUID();

        when(lessonApi.setActive(eq(id), eq(true)))
            .thenReturn(lessonResponse(id, subjectId));

        client.patch().uri("/api/lessons/{id}/active", id)
            .body(new SetActiveLessonRequest(true))
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").isEqualTo(id.toString());
    }

    @Test
    void setActive_false_returnsOk() {
        var id = UUID.randomUUID();
        var subjectId = UUID.randomUUID();

        when(lessonApi.setActive(eq(id), eq(false)))
            .thenReturn(lessonResponse(id, subjectId));

        client.patch().uri("/api/lessons/{id}/active", id)
            .body(new SetActiveLessonRequest(false))
            .exchange()
            .expectStatus().isOk();
    }

    @Test
    void setActive_notFound_returns404() {
        var id = UUID.randomUUID();
        when(lessonApi.setActive(eq(id), any(Boolean.class)))
            .thenThrow(new ResourceNotFoundException("Lesson", id));

        client.patch().uri("/api/lessons/{id}/active", id)
            .body(new SetActiveLessonRequest(true))
            .exchange()
            .expectStatus().isNotFound();
    }

    // -------------------------------------------------------------------------
    // DELETE /api/lessons/{id}
    // -------------------------------------------------------------------------

    @Test
    void deleteLesson_found_returns204() {
        var id = UUID.randomUUID();
        doNothing().when(lessonApi).deleteLesson(id);

        client.delete().uri("/api/lessons/{id}", id)
            .exchange()
            .expectStatus().isNoContent();
    }

    @Test
    void deleteLesson_notFound_returns404() {
        var id = UUID.randomUUID();
        doThrow(new ResourceNotFoundException("Lesson", id))
            .when(lessonApi).deleteLesson(id);

        client.delete().uri("/api/lessons/{id}", id)
            .exchange()
            .expectStatus().isNotFound();
    }

    // -------------------------------------------------------------------------
    // POST /api/lessons/bulk
    // -------------------------------------------------------------------------

    @Test
    void bulkCreate_returns201WithList() {
        var subjectId = UUID.randomUUID();
        var id1 = UUID.randomUUID();
        var id2 = UUID.randomUUID();
        var request = new BulkCreateLessonsRequest(subjectId, 1, 1);

        when(lessonApi.bulkCreate(any(BulkCreateLessonsRequest.class)))
            .thenReturn(List.of(lessonResponse(id1, subjectId), lessonResponse(id2, subjectId)));

        client.post().uri("/api/lessons/bulk")
            .body(request)
            .exchange()
            .expectStatus().isCreated()
            .expectBody()
            .jsonPath("$[0].id").isEqualTo(id1.toString())
            .jsonPath("$[1].id").isEqualTo(id2.toString());
    }

    @Test
    void bulkCreate_subjectNotFound_returns404() {
        var subjectId = UUID.randomUUID();
        when(lessonApi.bulkCreate(any(BulkCreateLessonsRequest.class)))
            .thenThrow(new ResourceNotFoundException("Subject", subjectId));

        client.post().uri("/api/lessons/bulk")
            .body(new BulkCreateLessonsRequest(subjectId, 1, 0))
            .exchange()
            .expectStatus().isNotFound();
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private static LessonResponse lessonResponse(UUID id, UUID subjectId) {
        return new LessonResponse(
            id,
            subjectId,
            "Subject Name",
            LessonType.LECTURE,
            1,
            "Topic",
            false,
            List.of(),
            List.of(),
            null,
            null
        );
    }
}
