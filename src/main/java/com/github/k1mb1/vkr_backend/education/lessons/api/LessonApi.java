package com.github.k1mb1.vkr_backend.education.lessons.api;

import com.github.k1mb1.vkr_backend.education.lessons.api.requests.*;
import com.github.k1mb1.vkr_backend.education.lessons.api.responses.LessonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping(value = "/api/lessons", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Lessons", description = "Lesson management")
public interface LessonApi {
    @Operation(summary = "List lessons")
    @GetMapping
    ResponseEntity<Page<LessonResponse>> findAll(
        @ParameterObject @ModelAttribute LessonFilterRequest filter,
        Pageable pageable
    );

    @Operation(summary = "Create lesson")
    @PostMapping
    ResponseEntity<LessonResponse> create(@RequestBody @Valid CreateLessonRequest request);

    @Operation(summary = "Bulk schedule lessons")
    @PostMapping("/bulk")
    ResponseEntity<List<LessonResponse>> bulkSchedule(@RequestBody @Valid BulkScheduleRequest request);

    @Operation(summary = "Update lesson")
    @PatchMapping("/{id}")
    ResponseEntity<LessonResponse> update(@PathVariable UUID id, @RequestBody @Valid UpdateLessonRequest request);

    @Operation(summary = "Issue a lesson manually")
    @PostMapping("/{id}/issue")
    ResponseEntity<LessonResponse> issue(@PathVariable UUID id);

    @Operation(summary = "Update issued task index")
    @PatchMapping("/{id}/issued-task-index")
    ResponseEntity<LessonResponse> updateIssuedTaskIndex(@PathVariable UUID id, @RequestBody @Valid UpdateIssuedTaskIndexRequest request);

    @Operation(summary = "Delete lesson")
    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@PathVariable UUID id);
}
