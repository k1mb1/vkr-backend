package com.github.k1mb1.vkr_backend.attendance.web;

import com.github.k1mb1.vkr_backend.attendance.AttendanceApi;
import com.github.k1mb1.vkr_backend.attendance.web.filters.AttendanceFilter;
import com.github.k1mb1.vkr_backend.attendance.web.requests.BulkUpsertAttendanceRequest;
import com.github.k1mb1.vkr_backend.attendance.web.responses.AttendanceCellResponse;
import com.github.k1mb1.vkr_backend.attendance.web.responses.AttendanceTableResponse;
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
    value = "/api/attendances", produces = MediaType.APPLICATION_JSON_VALUE
)
@Tag(name = "Attendances", description = "Посещаемость")
@RestController
@RequiredArgsConstructor
public class AttendanceController {

    final AttendanceApi attendanceApi;

    @Operation(
        summary = "Получить полную таблицу посещаемости по permissionId",
        description = "Таблица по всем занятиям предмета разрешения. Пер-урочный режим " +
            "(grades+attendance по одному занятию) — на GET /api/results?permissionId=&lessonId="
    )
    @GetMapping
    public ResponseEntity<AttendanceTableResponse> getAttendanceTable(
        @Parameter(
            description = "ID разрешения преподавателя (обязательно — определяет, что видно)",
            required = true
        )
        @RequestParam
        UUID permissionId
    ) {
        return ResponseEntity.ok(
            attendanceApi.getAttendanceTable(
                AttendanceFilter.builder().permissionId(permissionId).build()
            )
        );
    }

    @Operation(
        summary = "Массовое создание/обновление ячеек посещаемости"
    )
    @PutMapping
    public ResponseEntity<List<AttendanceCellResponse>> bulkUpsertAttendance(
        @Valid
        @RequestBody
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Список ячеек", required = true
        )
        BulkUpsertAttendanceRequest request
    ) {
        return ResponseEntity.ok(attendanceApi.upsertAll(request));
    }
}
