package com.github.k1mb1.vkr_backend.attendance.checkin.web;

import com.github.k1mb1.vkr_backend.attendance.checkin.CheckInSessionApi;
import com.github.k1mb1.vkr_backend.attendance.checkin.web.requests.StudentCheckInRequest;
import com.github.k1mb1.vkr_backend.attendance.checkin.web.responses.CheckInRecordResponse;
import com.github.k1mb1.vkr_backend.attendance.checkin.web.responses.PublicCheckInSessionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequestMapping(
    value = "/api/check-in-sessions/public", produces = MediaType.APPLICATION_JSON_VALUE
)
@Tag(
    name = "Check-in sessions (public)",
    description = "Публичная страница опроса по UUID сессии (QR-код)"
)
@RestController
@RequiredArgsConstructor
public class PublicCheckInController {

    final CheckInSessionApi checkInSessionApi;

    @Operation(summary = "Получить состояние сессии и список студентов")
    @GetMapping("/{id}")
    public ResponseEntity<PublicCheckInSessionResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(checkInSessionApi.getPublic(id));
    }

    @Operation(summary = "Отметиться студенту")
    @PostMapping("/{id}/check-in")
    public ResponseEntity<CheckInRecordResponse> checkIn(
        @PathVariable UUID id,
        @Valid
        @RequestBody
        StudentCheckInRequest request
    ) {
        return ResponseEntity.ok(checkInSessionApi.checkIn(id, request));
    }
}
