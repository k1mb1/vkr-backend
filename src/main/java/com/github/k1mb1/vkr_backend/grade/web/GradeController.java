package com.github.k1mb1.vkr_backend.grade.web;

import com.github.k1mb1.vkr_backend.grade.GradeApi;
import com.github.k1mb1.vkr_backend.grade.web.filters.GradeFilter;
import com.github.k1mb1.vkr_backend.grade.web.requests.UpsertGradeRequest;
import com.github.k1mb1.vkr_backend.grade.web.responses.GradeCellResponse;
import com.github.k1mb1.vkr_backend.grade.web.responses.GradeTableResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping(
    value = "/api/grades", produces = MediaType.APPLICATION_JSON_VALUE
)
@Tag(name = "Grades", description = "Оценки")
@RestController
@RequiredArgsConstructor
public class GradeController {

    final GradeApi gradeApi;

    @Operation(summary = "Получить таблицу оценок по permissionId")
    @GetMapping
    public ResponseEntity<GradeTableResponse> getGradeTable(
        @ParameterObject
        @Valid
        @ModelAttribute
        GradeFilter filter
    ) {
        return ResponseEntity.ok(gradeApi.getGradeTable(filter));
    }

    @Operation(summary = "Проставить или обновить оценку для пары (студент, задание)")
    @PutMapping
    public ResponseEntity<GradeCellResponse> upsert(
        @Valid
        @RequestBody
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Ячейка оценки", required = true
        )
        UpsertGradeRequest request
    ) {
        return ResponseEntity.ok(gradeApi.upsert(request));
    }
}
