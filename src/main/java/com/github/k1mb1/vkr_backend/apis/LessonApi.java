package com.github.k1mb1.vkr_backend.apis;

import com.github.k1mb1.vkr_backend.domain.lessons.LessonFilter;
import com.github.k1mb1.vkr_backend.domain.lessons.requests.CreateLessonRequest;
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

    @Operation(summary = "Get lesson by id")
    @GetMapping("/{id}")
    ResponseEntity<LessonResponse> findById(@PathVariable UUID id);

    @Operation(summary = "Create lesson")
    @PostMapping
    ResponseEntity<LessonResponse> create(@RequestBody @Valid CreateLessonRequest request);

    @Operation(summary = "Update lesson")
    @PatchMapping("/{id}")
    ResponseEntity<LessonResponse> update(@PathVariable UUID id, @RequestBody @Valid UpdateLessonRequest request);

    @Operation(summary = "Delete lesson")
    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@PathVariable UUID id);
}
