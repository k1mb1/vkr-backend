package com.github.k1mb1.vkr_backend.lesson.web;

import com.github.k1mb1.vkr_backend.lesson.LessonApi;
import com.github.k1mb1.vkr_backend.lesson.web.filters.LessonFilter;
import com.github.k1mb1.vkr_backend.lesson.web.requests.BulkScheduleRequest;
import com.github.k1mb1.vkr_backend.lesson.web.requests.CreateLessonsByTypeRequest;
import com.github.k1mb1.vkr_backend.lesson.web.requests.UpdateLessonRequest;
import com.github.k1mb1.vkr_backend.lesson.web.responses.LessonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping(
    value = "/api/lessons",
    produces = MediaType.APPLICATION_JSON_VALUE
)
@Tag(name = "Lessons", description = "Lesson management")
@RestController
@RequiredArgsConstructor
public class LessonController {

    final LessonApi lessonApi;

    @Operation(summary = "Get lessons page filtered by subject")
    @GetMapping
    public ResponseEntity<Page<LessonResponse>> getPage(
        @ModelAttribute LessonFilter filter,
        @ParameterObject Pageable pageable
    ) {
        return ResponseEntity.ok(lessonApi.getPage(filter, pageable));
    }

    @Operation(summary = "Partially update lesson")
    @PatchMapping("/{id}")
    public ResponseEntity<LessonResponse> update(
        @PathVariable UUID id,
        @Valid @RequestBody UpdateLessonRequest request
    ) {
        return ResponseEntity.ok(lessonApi.update(id, request));
    }

    @Operation(summary = "Delete lesson")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        lessonApi.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Bulk schedule lessons by weekly day pattern")
    @PostMapping("/bulk-schedule")
    public ResponseEntity<List<LessonResponse>> bulkSchedule(
        @Valid @RequestBody BulkScheduleRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(lessonApi.bulkSchedule(request));
    }

    @Operation(summary = "Create lessons by type count")
    @PostMapping("/by-type")
    public ResponseEntity<List<LessonResponse>> createByType(
        @Valid @RequestBody CreateLessonsByTypeRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(lessonApi.createByType(request));
    }
}
