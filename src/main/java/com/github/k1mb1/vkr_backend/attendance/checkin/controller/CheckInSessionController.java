package com.github.k1mb1.vkr_backend.attendance.checkin.controller;

import com.github.k1mb1.vkr_backend.attendance.checkin.service.CheckInSessionService;
import com.github.k1mb1.vkr_backend.attendance.checkin.service.dto.filter.CheckInSessionFilter;
import com.github.k1mb1.vkr_backend.attendance.checkin.service.dto.request.ConfirmCheckInRequest;
import com.github.k1mb1.vkr_backend.attendance.checkin.service.dto.request.StartCheckInRequest;
import com.github.k1mb1.vkr_backend.attendance.checkin.service.dto.response.CheckInPreviewResponse;
import com.github.k1mb1.vkr_backend.attendance.checkin.service.dto.response.CheckInSessionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping(value = "/api/check-in-sessions", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Check-in sessions", description = "Опросы по посещаемости (преподавательская сторона)")
@RestController
@RequiredArgsConstructor
public class CheckInSessionController {

    final CheckInSessionService checkInSessionService;

    @Operation(summary = "Запустить опрос по посещаемости на занятии")
    @PostMapping
    public ResponseEntity<CheckInSessionResponse> startCheckInSession(@Valid @RequestBody StartCheckInRequest request) {
        return ResponseEntity.ok(checkInSessionService.start(request));
    }

    @Operation(summary = "Получить состояние одной check-in сессии")
    @GetMapping("/{id}")
    public ResponseEntity<CheckInSessionResponse> getCheckInSession(
            @Parameter(description = "ID check-in сессии") @PathVariable UUID id) {
        return ResponseEntity.ok(checkInSessionService.get(id));
    }

    @Operation(summary = "Список check-in сессий с фильтрами по предмету разрешения / занятию / scope")
    @GetMapping
    public ResponseEntity<List<CheckInSessionResponse>> getCheckInSessions(
            @ParameterObject @Valid @ModelAttribute CheckInSessionFilter filter) {
        return ResponseEntity.ok(checkInSessionService.list(filter));
    }

    @Operation(summary = "Сверочный пред-просмотр (что попадёт в основную посещаемость)")
    @GetMapping("/{id}/preview")
    public ResponseEntity<CheckInPreviewResponse> previewCheckInSession(
            @Parameter(description = "ID check-in сессии") @PathVariable UUID id) {
        return ResponseEntity.ok(checkInSessionService.preview(id));
    }

    @Operation(summary = "Подтвердить и перенести результаты в основную посещаемость")
    @PostMapping("/{id}/confirm")
    public ResponseEntity<CheckInSessionResponse> confirmCheckInSession(
            @Parameter(description = "ID check-in сессии") @PathVariable UUID id,
            @Valid @RequestBody(required = false) ConfirmCheckInRequest request) {
        return ResponseEntity.ok(checkInSessionService.confirm(id, request));
    }

    @Operation(summary = "Отменить check-in сессию (без переноса в посещаемость)")
    @PostMapping("/{id}/cancel")
    public ResponseEntity<CheckInSessionResponse> cancelCheckInSession(
            @Parameter(description = "ID check-in сессии") @PathVariable UUID id) {
        return ResponseEntity.ok(checkInSessionService.cancel(id));
    }
}
