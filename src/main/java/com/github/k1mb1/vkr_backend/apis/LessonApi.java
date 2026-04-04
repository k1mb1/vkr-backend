package com.github.k1mb1.vkr_backend.apis;

import com.github.k1mb1.vkr_backend.domain.lessons.requests.BulkScheduleLessonsRequest;
import com.github.k1mb1.vkr_backend.domain.lessons.requests.CreateLessonRequest;
import com.github.k1mb1.vkr_backend.domain.lessons.responses.LessonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping(
    value = "/api/lessons",
    produces = MediaType.APPLICATION_JSON_VALUE
)
@Tag(name = "Lessons", description = "Lesson management")
public interface LessonApi {

    @Operation(summary = "List all lessons by subject")
    @GetMapping("/subjects/{subjectId}")
    ResponseEntity<List<LessonResponse>> findAllBySubjectId(
        @PathVariable UUID subjectId
    );

    @Operation(summary = "Create a single lesson")
    @PostMapping
    ResponseEntity<LessonResponse> create(
        @RequestBody @Valid CreateLessonRequest request
    );

    /**
     * Bulk-schedule recurring lessons from a timetable.
     *
     * <p>Lectures are generated with {@code groupId = null} (whole cohort).
     * Practices can optionally target a specific subgroup via {@code groupId}
     * in each {@link com.github.k1mb1.vkr_backend.domain.lessons.requests.LessonSlot}.
     */
    @Operation(summary = "Bulk-schedule recurring lessons")
    @PostMapping("/bulk-schedule")
    ResponseEntity<List<LessonResponse>> bulkSchedule(
        @RequestBody @Valid BulkScheduleLessonsRequest request
    );
}
