package com.github.k1mb1.vkr_backend.teacher.web;

import com.github.k1mb1.vkr_backend.teacher.TeachersApi;
import com.github.k1mb1.vkr_backend.teacher.web.requests.CreateOrUpdateTeacherRequest;
import com.github.k1mb1.vkr_backend.teacher.web.response.TeacherResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
