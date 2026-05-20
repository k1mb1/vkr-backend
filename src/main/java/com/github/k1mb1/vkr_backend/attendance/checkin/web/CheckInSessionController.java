package com.github.k1mb1.vkr_backend.attendance.checkin.web;

import com.github.k1mb1.vkr_backend.attendance.checkin.CheckInSessionApi;
import com.github.k1mb1.vkr_backend.attendance.checkin.web.requests.ConfirmCheckInRequest;
import com.github.k1mb1.vkr_backend.attendance.checkin.web.requests.StartCheckInRequest;
import com.github.k1mb1.vkr_backend.attendance.checkin.web.responses.CheckInPreviewResponse;
import com.github.k1mb1.vkr_backend.attendance.checkin.web.responses.CheckInSessionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequestMapping(
    value = "/api/check-in-sessions", produces = MediaType.APPLICATION_JSON_VALUE
)
@Tag(
    name = "Check-in sessions", description = "Опросы по посещаемости (преподавательская сторона)"
)
@RestController
@RequiredArgsConstructor
public class CheckInSessionController {

    final CheckInSessionApi checkInSessionApi;

    @Operation(summary = "Запустить опрос по посещаемости на занятии")
    @PostMapping
    public ResponseEntity<CheckInSessionResponse> start(
        @Valid
        @RequestBody
        StartCheckInRequest request
    ) {
        return ResponseEntity.ok(checkInSessionApi.start(request));
    }

    @Operation(summary = "Получить состояние одной check-in сессии")
    @GetMapping("/{id}")
    public ResponseEntity<CheckInSessionResponse> get(
        @PathVariable UUID id
    ) {
        return ResponseEntity.ok(checkInSessionApi.get(id));
    }

    @Operation(summary = "Список check-in сессий по permissionId (по предмету разрешения)")
    @GetMapping
    public ResponseEntity<List<CheckInSessionResponse>> list(
        @RequestParam UUID permissionId
    ) {
        return ResponseEntity.ok(checkInSessionApi.listForPermission(permissionId));
    }

    @Operation(
        summary = "Сверочный пред-просмотр (что попадёт в основную посещаемость)"
    )
    @GetMapping("/{id}/preview")
    public ResponseEntity<CheckInPreviewResponse> preview(
        @PathVariable UUID id
    ) {
        return ResponseEntity.ok(checkInSessionApi.preview(id));
    }

    @Operation(
        summary = "Подтвердить и перенести результаты в основную посещаемость"
    )
    @PostMapping("/{id}/confirm")
    public ResponseEntity<CheckInSessionResponse> confirm(
        @PathVariable UUID id,
        @Valid
        @RequestBody(required = false)
        ConfirmCheckInRequest request
    ) {
        return ResponseEntity.ok(checkInSessionApi.confirm(id, request));
    }

    @Operation(summary = "Отменить check-in сессию (без переноса в посещаемость)")
    @PostMapping("/{id}/cancel")
    public ResponseEntity<CheckInSessionResponse> cancel(
        @PathVariable UUID id
    ) {
        return ResponseEntity.ok(checkInSessionApi.cancel(id));
    }
}
