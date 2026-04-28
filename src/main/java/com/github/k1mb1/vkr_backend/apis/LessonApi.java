package com.github.k1mb1.vkr_backend.apis;

import com.github.k1mb1.vkr_backend.domain.lessons.LessonFilter;
import com.github.k1mb1.vkr_backend.domain.lessons.requests.BulkScheduleRequest;
import com.github.k1mb1.vkr_backend.domain.lessons.requests.CreateLessonRequest;
import com.github.k1mb1.vkr_backend.domain.lessons.requests.UpdateIssuedTaskIndexRequest;
import com.github.k1mb1.vkr_backend.domain.lessons.requests.UpdateLessonRequest;
import com.github.k1mb1.vkr_backend.domain.lessons.responses.LessonResponse;
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

@RequestMapping(
    value = "/api/lessons",
    produces = MediaType.APPLICATION_JSON_VALUE
)
@Tag(name = "Lessons", description = "Lesson management")
public interface LessonApi {

    @Operation(summary = "List lessons (filter by subjectId)")
    @GetMapping
    ResponseEntity<Page<LessonResponse>> findAll(
        @ParameterObject @ModelAttribute LessonFilter filter,
        @ParameterObject Pageable pageable
    );

    @Operation(summary = "Create a single lesson")
    @PostMapping
    ResponseEntity<LessonResponse> create(
        @RequestBody @Valid CreateLessonRequest request
    );

    @Operation(summary = "Bulk-schedule recurring lessons")
    @PostMapping("/bulk-schedule")
    ResponseEntity<List<LessonResponse>> bulkSchedule(
        @RequestBody @Valid BulkScheduleRequest request
    );

    @Operation(summary = "Partial update of lesson metadata")
    @PatchMapping("/{id}")
    ResponseEntity<LessonResponse> update(
        @PathVariable UUID id,
        @RequestBody @Valid UpdateLessonRequest request
    );

    @Operation(summary = "Delete a lesson permanently")
    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@PathVariable UUID id);

    @Operation(
        summary = "Manually issue a lesson",
        description = "Only valid for lessons with issuanceMode=MANUAL. Idempotent."
    )
    @PostMapping("/{id}/issue")
    ResponseEntity<LessonResponse> issueLesson(@PathVariable UUID id);

    @Operation(
        summary = "Update the issued task index",
        description = "Advances which task is currently active. Tasks with position < issuedTaskIndex are superseded and receive a displacement penalty on the front-end."
    )
    @PatchMapping("/{id}/issued-task-index")
    ResponseEntity<LessonResponse> updateIssuedTaskIndex(
        @PathVariable UUID id,
        @RequestBody @Valid UpdateIssuedTaskIndexRequest request
    );
}
