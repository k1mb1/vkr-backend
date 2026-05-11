package com.github.k1mb1.vkr_backend.lesson.web;

import com.github.k1mb1.vkr_backend.common.error.ErrorDto;
import com.github.k1mb1.vkr_backend.lesson.LessonApi;
import com.github.k1mb1.vkr_backend.lesson.web.filters.LessonFilter;
import com.github.k1mb1.vkr_backend.lesson.web.requests.BulkScheduleRequest;
import com.github.k1mb1.vkr_backend.lesson.web.requests.CreateLessonsByTypeRequest;
import com.github.k1mb1.vkr_backend.lesson.web.requests.UpdateLessonRequest;
import com.github.k1mb1.vkr_backend.lesson.web.responses.LessonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@Tag(name = "Lessons", description = "Управление занятиями и расписанием")
@RestController
@RequiredArgsConstructor
public class LessonController {

    final LessonApi lessonApi;

    @Operation(summary = "Получить страницу занятий с фильтрацией по предмету")
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
    public ResponseEntity<Page<LessonResponse>> getPage(
        @ParameterObject @ModelAttribute LessonFilter filter,
        @ParameterObject Pageable pageable
    ) {
        return ResponseEntity.ok(lessonApi.getPage(filter, pageable));
    }

    @Operation(summary = "Частично обновить занятие")
    @ApiResponses(
        {
            @ApiResponse(
                responseCode = "200",
                description = "Занятие обновлено"
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
                description = "Занятие не найдено",
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
    public ResponseEntity<LessonResponse> update(
        @Parameter(
            description = "ID занятия",
            example = "550e8400-e29b-41d4-a716-446655440000"
        ) @PathVariable UUID id,
        @Valid @RequestBody @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Данные для обновления занятия",
            required = true
        ) UpdateLessonRequest request
    ) {
        return ResponseEntity.ok(lessonApi.update(id, request));
    }

    @Operation(summary = "Удалить занятие")
    @ApiResponses(
        {
            @ApiResponse(responseCode = "204", description = "Занятие удалено"),
            @ApiResponse(
                responseCode = "404",
                description = "Занятие не найдено",
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
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
        @Parameter(
            description = "ID занятия",
            example = "550e8400-e29b-41d4-a716-446655440000"
        ) @PathVariable UUID id
    ) {
        lessonApi.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Массовое создание занятий по недельному шаблону")
    @ApiResponses(
        {
            @ApiResponse(responseCode = "201", description = "Занятия созданы"),
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
    @PostMapping("/bulk-schedule")
    public ResponseEntity<List<LessonResponse>> bulkSchedule(
        @Valid @RequestBody @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Шаблон расписания",
            required = true
        ) BulkScheduleRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
            lessonApi.bulkSchedule(request)
        );
    }

    @Operation(summary = "Создать занятия по количеству типов")
    @ApiResponses(
        {
            @ApiResponse(responseCode = "201", description = "Занятия созданы"),
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
    @PostMapping("/by-type")
    public ResponseEntity<List<LessonResponse>> createByType(
        @Valid @RequestBody @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Количество занятий по типам",
            required = true
        ) CreateLessonsByTypeRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
            lessonApi.createByType(request)
        );
    }
}
