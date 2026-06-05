package com.github.k1mb1.vkr_backend.grading.web;

import com.github.k1mb1.vkr_backend.grading.GradingApi;
import com.github.k1mb1.vkr_backend.grading.web.filters.GradingFilter;
import com.github.k1mb1.vkr_backend.grading.web.requests.BulkUpsertGradesRequest;
import com.github.k1mb1.vkr_backend.grading.web.responses.GradeCellResponse;
import com.github.k1mb1.vkr_backend.grading.web.responses.GradingTableResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequestMapping(
    value = "/api/grades", produces = MediaType.APPLICATION_JSON_VALUE
)
@Tag(name = "Grades", description = "Оценивание")
@RestController
@RequiredArgsConstructor
public class GradingController {

    final GradingApi gradingApi;

    @Operation(
        summary = "Получить полную таблицу оценок по permissionId",
        description = "Таблица по всем занятиям предмета разрешения. Пер-урочный режим " +
            "(grades+attendance по одному занятию) — на GET /api/results?permissionId=&lessonId="
    )
    @GetMapping
    public ResponseEntity<GradingTableResponse> getGradingTable(
        @Parameter(
            description = "ID разрешения преподавателя (обязательно — определяет, что видно)",
            required = true
        )
        @RequestParam
        UUID permissionId
    ) {
        return ResponseEntity.ok(
            gradingApi.getGradingTable(
                GradingFilter.builder().permissionId(permissionId).build()
            )
        );
    }

    @Operation(
        summary = "Массовое создание/обновление ячеек оценок"
    )
    @PutMapping
    public ResponseEntity<List<GradeCellResponse>> upsertGrades(
        @Valid
        @RequestBody
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Список ячеек оценок", required = true
        )
        BulkUpsertGradesRequest request
    ) {
        return ResponseEntity.ok(gradingApi.upsertGrades(request));
    }
}
