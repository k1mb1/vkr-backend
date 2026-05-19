package com.github.k1mb1.vkr_backend.attendance.web;

import com.github.k1mb1.vkr_backend.attendance.AttendanceApi;
import com.github.k1mb1.vkr_backend.attendance.web.filters.AttendanceFilter;
import com.github.k1mb1.vkr_backend.attendance.web.requests.UpsertAttendanceRequest;
import com.github.k1mb1.vkr_backend.attendance.web.responses.AttendanceCellResponse;
import com.github.k1mb1.vkr_backend.attendance.web.responses.AttendanceTableResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping(
    value = "/api/attendances", produces = MediaType.APPLICATION_JSON_VALUE
)
@Tag(name = "Attendances", description = "Посещаемость")
@RestController
@RequiredArgsConstructor
public class AttendanceController {

    final AttendanceApi attendanceApi;

    @Operation(
        summary = "Получить таблицу посещаемости по permissionId"
    )
    @GetMapping
    public ResponseEntity<AttendanceTableResponse> getAttendanceTable(
        @ParameterObject
        @Valid
        @ModelAttribute
        AttendanceFilter filter
    ) {
        return ResponseEntity.ok(attendanceApi.getAttendanceTable(filter));
    }

    @Operation(
        summary = "Проставить или обновить отметку для пары (студент, занятие)"
    )
    @PutMapping
    public ResponseEntity<AttendanceCellResponse> upsert(
        @Valid
        @RequestBody
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Ячейка посещаемости", required = true
        )
        UpsertAttendanceRequest request
    ) {
        return ResponseEntity.ok(attendanceApi.upsert(request));
    }
}
