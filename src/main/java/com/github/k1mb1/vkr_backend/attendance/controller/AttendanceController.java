package com.github.k1mb1.vkr_backend.attendance.controller;

import com.github.k1mb1.vkr_backend.attendance.api.AttendanceCellResponse;
import com.github.k1mb1.vkr_backend.attendance.api.AttendanceTableResponse;
import com.github.k1mb1.vkr_backend.attendance.service.AttendanceService;
import com.github.k1mb1.vkr_backend.attendance.service.dto.filter.AttendanceFilter;
import com.github.k1mb1.vkr_backend.attendance.service.dto.request.BulkUpsertAttendanceRequest;
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

@RequestMapping(value = "/api/attendances", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Attendances", description = "Посещаемость")
@RestController
@RequiredArgsConstructor
public class AttendanceController {

    final AttendanceService attendanceService;

    @Operation(
            summary = "Получить полную таблицу посещаемости по permissionId",
            description = "Таблица по всем занятиям предмета разрешения. Пер-урочный режим "
                    + "(grades+attendance по одному занятию) — на GET /api/results?permissionId=&lessonId=")
    @GetMapping
    public ResponseEntity<AttendanceTableResponse> getAttendanceTable(
            @Parameter(
                            description = "ID разрешения преподавателя (обязательно — определяет, что видно)",
                            required = true)
                    @RequestParam
                    UUID permissionId) {
        return ResponseEntity.ok(attendanceService.getAttendanceTable(
                AttendanceFilter.builder().permissionId(permissionId).build()));
    }

    @Operation(summary = "Массовое создание/обновление ячеек посещаемости")
    @PutMapping
    public ResponseEntity<List<AttendanceCellResponse>> updateAttendances(
            @Valid @RequestBody
                    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Список ячеек", required = true)
                    BulkUpsertAttendanceRequest request) {
        return ResponseEntity.ok(attendanceService.upsertAll(request));
    }
}
