package com.github.k1mb1.vkr_backend.results.controller;

import com.github.k1mb1.vkr_backend.attendance.api.AttendanceApi;
import com.github.k1mb1.vkr_backend.attendance.service.dto.filter.AttendanceFilter;
import com.github.k1mb1.vkr_backend.grading.api.GradingApi;
import com.github.k1mb1.vkr_backend.grading.service.dto.filter.GradingFilter;
import com.github.k1mb1.vkr_backend.results.service.dto.filter.ResultsFilter;
import com.github.k1mb1.vkr_backend.results.service.dto.response.ResultsResponse;
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

    final GradingApi gradingApi;

    final AttendanceApi attendanceApi;

    @Operation(summary = "Получить итоги по permissionId одним запросом (оценки + посещаемость)")
    @GetMapping
    public ResponseEntity<ResultsResponse> getResults(@ParameterObject @Valid @ModelAttribute ResultsFilter filter) {
        var grading = gradingApi.getGradingTable(GradingFilter.builder()
                .permissionId(filter.permissionId())
                .lessonId(filter.lessonId())
                .build());
        var attendance = attendanceApi.getAttendanceTable(AttendanceFilter.builder()
                .permissionId(filter.permissionId())
                .lessonId(filter.lessonId())
                .build());
        return ResponseEntity.ok(ResultsResponse.builder()
                .grading(grading)
                .attendance(attendance)
                .build());
    }
}
