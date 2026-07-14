package com.github.k1mb1.vkr_backend.subject.controller;

import com.github.k1mb1.vkr_backend.common.web.ErrorDto;
import com.github.k1mb1.vkr_backend.subject.service.TeacherService;
import com.github.k1mb1.vkr_backend.subject.service.dto.filter.TeacherFilter;
import com.github.k1mb1.vkr_backend.subject.service.dto.request.CreateOrUpdateTeacherRequest;
import com.github.k1mb1.vkr_backend.subject.service.dto.response.TeacherResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping(value = "/api/teachers", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Teachers", description = "Управление преподавателями")
@RestController
@RequiredArgsConstructor
public class TeachersController {

    final TeacherService teacherService;

    @Operation(summary = "Создать или обновить преподавателя")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Преподаватель создан или обновлен"),
        @ApiResponse(
                responseCode = "400",
                description = "Ошибка валидации",
                content = @Content(schema = @Schema(implementation = ErrorDto.class))),
    })
    @PutMapping("/{id}")
    public ResponseEntity<TeacherResponse> updateTeacher(
            @Parameter(description = "ID преподавателя") @PathVariable UUID id,
            @Valid @RequestBody
                    @io.swagger.v3.oas.annotations.parameters.RequestBody(
                            description = "Данные преподавателя",
                            required = true)
                    CreateOrUpdateTeacherRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(teacherService.createOrUpdateTeacher(id, request));
    }

    @Operation(summary = "Получить страницу преподавателей с фильтрацией по имени")
    @GetMapping
    public ResponseEntity<Page<TeacherResponse>> getTeachersPage(
            @Valid @ParameterObject @ModelAttribute TeacherFilter filter, @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(teacherService.getPage(filter, pageable));
    }
}
