package com.github.k1mb1.vkr_backend.apis;

import com.github.k1mb1.vkr_backend.domain.lessons.requests.BulkScheduleLessonsRequest;
import com.github.k1mb1.vkr_backend.domain.lessons.requests.CreateLessonRequest;
import com.github.k1mb1.vkr_backend.domain.lessons.requests.CreateLessonsByTypeRequest;
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
}
