package com.github.k1mb1.vkr_backend.journal.controller;

import com.github.k1mb1.vkr_backend.journal.service.GradingService;
import com.github.k1mb1.vkr_backend.journal.service.dto.filter.GradingFilter;
import com.github.k1mb1.vkr_backend.journal.service.dto.request.BulkUpsertGradesRequest;
import com.github.k1mb1.vkr_backend.journal.service.dto.response.GradeCellResponse;
import com.github.k1mb1.vkr_backend.journal.service.dto.response.GradingTableResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping(value = "/api/grades", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Grades", description = "Оценивание")
@RestController
@RequiredArgsConstructor
public class GradingController {

    final GradingService gradingService;

    @Operation(
            summary = "Получить полную таблицу оценок по permissionId",
            description = "Таблица по всем занятиям предмета разрешения. Пер-урочный режим "
                    + "(grades+attendance по одному занятию) — на GET /api/results?permissionId=&lessonId=")
    @GetMapping
    public ResponseEntity<GradingTableResponse> getGradingTable(
            @Parameter(
                            description = "ID разрешения преподавателя (обязательно — определяет, что видно)",
                            required = true)
                    @RequestParam
                    UUID permissionId) {
        return ResponseEntity.ok(gradingService.getGradingTable(
                GradingFilter.builder().permissionId(permissionId).build()));
    }

    @Operation(summary = "Массовое создание/обновление ячеек оценок")
    @PutMapping
    public ResponseEntity<List<GradeCellResponse>> updateGrades(
            @Valid @RequestBody
                    @io.swagger.v3.oas.annotations.parameters.RequestBody(
                            description = "Список ячеек оценок",
                            required = true)
                    BulkUpsertGradesRequest request) {
        return ResponseEntity.ok(gradingService.upsertGrades(request));
    }
}
