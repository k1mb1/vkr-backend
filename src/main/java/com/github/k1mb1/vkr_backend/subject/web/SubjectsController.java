package com.github.k1mb1.vkr_backend.subject.web;

import com.github.k1mb1.vkr_backend.grading.GradingApi;
import com.github.k1mb1.vkr_backend.subject.SubjectsApi;
import com.github.k1mb1.vkr_backend.subject.web.filters.SubjectFilter;
import com.github.k1mb1.vkr_backend.subject.web.requests.CreateSubjectRequest;
import com.github.k1mb1.vkr_backend.subject.web.requests.UpdateSubjectRequest;
import com.github.k1mb1.vkr_backend.subject.web.responses.SubjectPageResponse;
import com.github.k1mb1.vkr_backend.subject.web.responses.SubjectResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequestMapping(
    value = "/api/subjects", produces = MediaType.APPLICATION_JSON_VALUE
)
@Tag(name = "Subjects", description = "Управление предметами")
@RestController
@RequiredArgsConstructor
public class SubjectsController {

    final SubjectsApi subjectsApi;

    @Operation(summary = "Частично обновить предмет")
    @PatchMapping("/{id}")
    public ResponseEntity<SubjectResponse> updateSubject(
        @Parameter(description = "ID предмета")
        @PathVariable
        UUID id,
        @Valid
        @RequestBody
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Данные для обновления предмета", required = true
        )
        UpdateSubjectRequest request
    ) {
        return ResponseEntity.ok(subjectsApi.updateSubject(id, request));
    }

    @Operation(summary = "Создать предмет")
    @PostMapping
    public ResponseEntity<SubjectResponse> createSubject(
        @Valid
        @RequestBody
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Данные для создания предмета", required = true
        )
        CreateSubjectRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(subjectsApi.createSubject(request));
    }

    @Operation(summary = "Получить страницу предметов с фильтрацией по названию")
    @GetMapping
    public ResponseEntity<Page<SubjectPageResponse>> getSubjectsPage(
        @ParameterObject
        @ModelAttribute
        SubjectFilter filter,
        @ParameterObject Pageable pageable
    ) {
        return ResponseEntity.ok(subjectsApi.getPage(filter, pageable));
    }
}
