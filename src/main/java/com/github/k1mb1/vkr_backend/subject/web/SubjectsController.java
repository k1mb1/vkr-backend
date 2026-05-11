package com.github.k1mb1.vkr_backend.subject.web;

import com.github.k1mb1.vkr_backend.common.error.ErrorDto;
import com.github.k1mb1.vkr_backend.subject.SubjectsApi;
import com.github.k1mb1.vkr_backend.subject.web.filters.SubjectFilter;
import com.github.k1mb1.vkr_backend.subject.web.requests.CreateSubjectRequest;
import com.github.k1mb1.vkr_backend.subject.web.requests.UpdateSubjectRequest;
import com.github.k1mb1.vkr_backend.subject.web.responses.SubjectPageResponse;
import com.github.k1mb1.vkr_backend.subject.web.responses.SubjectResponse;
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
import org.springframework.web.bind.annotation.*;

@RequestMapping(
    value = "/api/subjects",
    produces = MediaType.APPLICATION_JSON_VALUE
)
@Tag(name = "Subjects", description = "Управление предметами")
@RestController
@RequiredArgsConstructor
public class SubjectsController {

    final SubjectsApi subjectsApi;

    @Operation(
        summary = "Получить страницу предметов с фильтрацией по названию"
    )
    @ApiResponses(
        {
            @ApiResponse(responseCode = "200", description = "Успешно"),
            @ApiResponse(
                responseCode = "500",
                description = "Внутренняя ошибка сервера",
                content = @Content(
                    schema = @Schema(implementation = ErrorDto.class)
                )
            ),
        }
    )
    @GetMapping
    public ResponseEntity<Page<SubjectPageResponse>> getPage(
        @ParameterObject @ModelAttribute SubjectFilter filter,
        @ParameterObject Pageable pageable
    ) {
        return ResponseEntity.ok(subjectsApi.getPage(filter, pageable));
    }

    @Operation(summary = "Частично обновить предмет")
    @ApiResponses(
        {
            @ApiResponse(
                responseCode = "200",
                description = "Предмет обновлен"
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Ошибка валидации",
                content = @Content(
                    schema = @Schema(implementation = ErrorDto.class)
                )
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Предмет не найден",
                content = @Content(
                    schema = @Schema(implementation = ErrorDto.class)
                )
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Внутренняя ошибка сервера",
                content = @Content(
                    schema = @Schema(implementation = ErrorDto.class)
                )
            ),
        }
    )
    @PatchMapping("/{id}")
    public ResponseEntity<SubjectResponse> update(
        @Parameter(
            description = "ID предмета",
            example = "550e8400-e29b-41d4-a716-446655440002"
        ) @PathVariable UUID id,
        @Valid @RequestBody @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Данные для обновления предмета",
            required = true
        ) UpdateSubjectRequest request
    ) {
        return ResponseEntity.ok(subjectsApi.update(id, request));
    }

    @Operation(summary = "Создать предмет")
    @ApiResponses(
        {
            @ApiResponse(responseCode = "201", description = "Предмет создан"),
            @ApiResponse(
                responseCode = "400",
                description = "Ошибка валидации",
                content = @Content(
                    schema = @Schema(implementation = ErrorDto.class)
                )
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Внутренняя ошибка сервера",
                content = @Content(
                    schema = @Schema(implementation = ErrorDto.class)
                )
            ),
        }
    )
    @PostMapping
    public ResponseEntity<SubjectResponse> create(
        @Valid @RequestBody @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Данные для создания предмета",
            required = true
        ) CreateSubjectRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
            subjectsApi.create(request)
        );
    }
}
