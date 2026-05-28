package com.github.k1mb1.vkr_backend.grading.web;

import com.github.k1mb1.vkr_backend.grading.GradingApi;
import com.github.k1mb1.vkr_backend.grading.web.filters.GradingFilter;
import com.github.k1mb1.vkr_backend.grading.web.requests.BulkUpsertGradesRequest;
import com.github.k1mb1.vkr_backend.grading.web.responses.GradeCellResponse;
import com.github.k1mb1.vkr_backend.grading.web.responses.GradingTableResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping(
    value = "/api/grades", produces = MediaType.APPLICATION_JSON_VALUE
)
@Tag(name = "Grades", description = "Оценивание")
@RestController
@RequiredArgsConstructor
public class GradingController {

    final GradingApi gradingApi;

    @Operation(
        summary = "Получить таблицу оценок по permissionId"
    )
    @GetMapping
    public ResponseEntity<GradingTableResponse> getGradingTable(
        @ParameterObject
        @Valid
        @ModelAttribute
        GradingFilter filter
    ) {
        return ResponseEntity.ok(gradingApi.getGradingTable(filter));
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
