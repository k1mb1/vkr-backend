package com.github.k1mb1.vkr_backend.journal.controller;

import com.github.k1mb1.vkr_backend.journal.service.AttendanceService;
import com.github.k1mb1.vkr_backend.journal.service.GradingService;
import com.github.k1mb1.vkr_backend.journal.service.dto.filter.ResultsFilter;
import com.github.k1mb1.vkr_backend.journal.service.dto.response.ResultsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping(value = "/api/results", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Results", description = "Итоги семестра (оценки + посещаемость одним запросом)")
@RestController
@RequiredArgsConstructor
public class ResultsController {

    final GradingService gradingService;

    final AttendanceService attendanceService;

    @Operation(summary = "Получить итоги по permissionId одним запросом (оценки + посещаемость)")
    @GetMapping
    public ResponseEntity<ResultsResponse> getResults(@ParameterObject @Valid @ModelAttribute ResultsFilter filter) {
        var grading = gradingService.getGradingTable(filter.permissionId(), null, filter.lessonId());
        var attendance = attendanceService.getAttendanceTable(filter.permissionId(), null, filter.lessonId());
        return ResponseEntity.ok(ResultsResponse.builder()
                .grading(grading)
                .attendance(attendance)
                .build());
    }
}
