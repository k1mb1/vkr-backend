package com.github.k1mb1.vkr_backend.subject.controller;

import com.github.k1mb1.vkr_backend.subject.service.SubjectService;
import com.github.k1mb1.vkr_backend.subject.service.dto.filter.SubjectFilter;
import com.github.k1mb1.vkr_backend.subject.service.dto.request.CreateSubjectRequest;
import com.github.k1mb1.vkr_backend.subject.service.dto.request.UpdateSubjectRequest;
import com.github.k1mb1.vkr_backend.subject.service.dto.response.SubjectPageResponse;
import com.github.k1mb1.vkr_backend.subject.service.dto.response.SubjectResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping(value = "/api/subjects", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Subjects", description = "Управление предметами")
@RestController
@RequiredArgsConstructor
public class SubjectsController {

    final SubjectService subjectService;

    @Operation(summary = "Частично обновить предмет")
    @PatchMapping("/{id}")
    public ResponseEntity<SubjectResponse> updateSubject(
            @Parameter(description = "ID предмета") @PathVariable UUID id,
            @Valid @RequestBody
                    @io.swagger.v3.oas.annotations.parameters.RequestBody(
                            description = "Данные для обновления предмета",
                            required = true)
                    UpdateSubjectRequest request) {
        return ResponseEntity.ok(subjectService.updateSubject(id, request));
    }

    @Operation(summary = "Создать предмет")
    @PostMapping
    public ResponseEntity<SubjectResponse> createSubject(
            @Valid @RequestBody
                    @io.swagger.v3.oas.annotations.parameters.RequestBody(
                            description = "Данные для создания предмета",
                            required = true)
                    CreateSubjectRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(subjectService.createSubject(request));
    }

    @Operation(summary = "Получить страницу предметов с фильтрацией по названию")
    @GetMapping
    public ResponseEntity<Page<SubjectPageResponse>> getSubjectsPage(
            @Valid @ParameterObject @ModelAttribute SubjectFilter filter, @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(subjectService.getPage(filter, pageable));
    }
}
