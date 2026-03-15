package com.github.k1mb1.vkr_backend.apis;

import com.github.k1mb1.vkr_backend.domain.lessons.LessonFilter;
import com.github.k1mb1.vkr_backend.domain.lessons.requests.CreateLessonRequest;
import com.github.k1mb1.vkr_backend.domain.lessons.requests.CreateLessonsByTypeRequest;
import com.github.k1mb1.vkr_backend.domain.lessons.requests.UpdateLessonRequest;
import com.github.k1mb1.vkr_backend.domain.lessons.responses.LessonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequestMapping(
        value = "/api/lessons",
        produces = MediaType.APPLICATION_JSON_VALUE
)
@Tag(name = "Lessons", description = "Lesson management")
public interface LessonApi {

    @Operation(summary = "List all lessons")
    @GetMapping
    ResponseEntity<Page<LessonResponse>> findAll(
            @ParameterObject @ModelAttribute LessonFilter filter,
            @ParameterObject Pageable pageable
    );

    @Operation(summary = "List all lessons by subject")
    @GetMapping("/subjects/{subjectId}")
    ResponseEntity<List<LessonResponse>> findAllBySubjectId(@PathVariable UUID subjectId);

    @Operation(summary = "List all lessons for a student (via enrolled subjects)")
    @GetMapping("/students/{studentId}")
    ResponseEntity<List<LessonResponse>> findAllByStudentId(@PathVariable UUID studentId);

    @Operation(summary = "Get lesson by id")
    @GetMapping("/{id}")
    ResponseEntity<LessonResponse> findById(@PathVariable UUID id);

    @Operation(summary = "Create lesson")
    @PostMapping
    ResponseEntity<LessonResponse> create(@RequestBody @Valid CreateLessonRequest request);

        @Operation(summary = "Bulk create lessons by type")
        @PostMapping("/by-type")
        ResponseEntity<List<LessonResponse>> createByType(@RequestBody @Valid CreateLessonsByTypeRequest request);

    @Operation(summary = "Update lesson")
    @PatchMapping("/{id}")
    ResponseEntity<LessonResponse> update(@PathVariable UUID id, @RequestBody @Valid UpdateLessonRequest request);

    @Operation(summary = "Delete lesson")
    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@PathVariable UUID id);
}
