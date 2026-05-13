package com.github.k1mb1.vkr_backend.teacher.web;

import com.github.k1mb1.vkr_backend.common.error.ErrorDto;
import com.github.k1mb1.vkr_backend.teacher.TeachersApi;
import com.github.k1mb1.vkr_backend.teacher.web.requests.CreateOrUpdateTeacherRequest;
import com.github.k1mb1.vkr_backend.teacher.web.response.TeacherResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequestMapping(
    value = "/api/teachers", produces = MediaType.APPLICATION_JSON_VALUE
)
@Tag(name = "Teachers", description = "Управление преподавателями")
@RestController
@RequiredArgsConstructor
public class TeachersController {

    final TeachersApi teachersApi;

    @Operation(summary = "Создать или обновить преподавателя")
    @ApiResponses(
        {
            @ApiResponse(
                responseCode = "200", description = "Преподаватель создан или обновлен"
            ), @ApiResponse(
            responseCode = "400", description = "Ошибка валидации", content = @Content(
            schema = @Schema(implementation = ErrorDto.class)
        )
        ),
        }
    )
    @PutMapping("/{id}")
    public ResponseEntity<TeacherResponse> createOrUpdateTeacher(
        @Parameter(description = "ID преподавателя")
        @PathVariable
        UUID id,
        @Valid
        @RequestBody
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Данные преподавателя", required = true
        )
        CreateOrUpdateTeacherRequest request
    ) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(teachersApi.createOrUpdateTeacher(id, request));
    }
}
