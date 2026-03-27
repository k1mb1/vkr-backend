package com.github.k1mb1.vkr_backend.apis;

import com.github.k1mb1.vkr_backend.domain.lessons.requests.BulkScheduleLessonsRequest;
import com.github.k1mb1.vkr_backend.domain.lessons.requests.CreateLessonRequest;
import com.github.k1mb1.vkr_backend.domain.lessons.requests.CreateLessonsByTypeRequest;
import com.github.k1mb1.vkr_backend.domain.lessons.requests.UpdateLessonRequest;
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

    @Operation(summary = "Create lesson")
    @PostMapping
    ResponseEntity<LessonResponse> create(
        @RequestBody @Valid CreateLessonRequest request
    );

    @Operation(summary = "Bulk create lessons by type")
    @PostMapping("/by-type")
    ResponseEntity<List<LessonResponse>> createByType(
        @RequestBody @Valid CreateLessonsByTypeRequest request
    );

    @Operation(
        summary = "Bulk schedule lessons with recurrence rules",
        description = """
            Generate lessons using recurring schedules (weekly / monthly).
            Each entry in `schedules` defines:
            - `type` — LECTURE or PRACTICE
            - `recurrence` — WEEKLY or MONTHLY
            - `daysOfWeek` — list of days (e.g. MONDAY, WEDNESDAY)
            - `time` — lesson start time (HH:mm)
            - `startDate` — date of first occurrence
            - `intervalWeeks` / `intervalMonths` — repeat every N weeks or months
            - `endDate` OR `totalCount` — when to stop (exactly one required)

            Two lessons on the same day: add two entries with different `time` values.
            Generated names get an ordinal suffix, e.g. "Лекция 3 (2)" for the 2nd lecture that day.
            """
    )
    @PostMapping("/bulk-schedule")
    ResponseEntity<List<LessonResponse>> bulkSchedule(
        @RequestBody @Valid BulkScheduleLessonsRequest request
    );

    @Operation(summary = "Update lesson")
    @PatchMapping("/{id}")
    ResponseEntity<LessonResponse> update(
        @PathVariable UUID id,
        @RequestBody @Valid UpdateLessonRequest request
    );

    @Operation(summary = "Delete lesson")
    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@PathVariable UUID id);
}
