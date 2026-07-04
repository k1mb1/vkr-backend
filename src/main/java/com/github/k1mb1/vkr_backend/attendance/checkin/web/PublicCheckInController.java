package com.github.k1mb1.vkr_backend.attendance.checkin.web;

import com.github.k1mb1.vkr_backend.attendance.checkin.CheckInRecordsApi;
import com.github.k1mb1.vkr_backend.attendance.checkin.CheckInSessionApi;
import com.github.k1mb1.vkr_backend.attendance.checkin.web.requests.PublicStudentSearchRequest;
import com.github.k1mb1.vkr_backend.attendance.checkin.web.requests.StudentCheckInRequest;
import com.github.k1mb1.vkr_backend.attendance.checkin.web.requests.VerifyCheckInCodeRequest;
import com.github.k1mb1.vkr_backend.attendance.checkin.web.responses.PublicCheckInRecordResponse;
import com.github.k1mb1.vkr_backend.attendance.checkin.web.responses.PublicCheckInSessionResponse;
import com.github.k1mb1.vkr_backend.attendance.checkin.web.responses.PublicStudentResponse;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping(value = "/api/check-in-sessions/public", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Check-in sessions (public)", description = "Публичная страница опроса по UUID сессии (QR-код)")
@RestController
@RequiredArgsConstructor
public class PublicCheckInController {

    final CheckInSessionApi checkInSessionApi;

    final CheckInRecordsApi checkInRecordsApi;

    @Operation(
            summary = "Получить состояние сессии",
            description = "Возвращает только метаданные сессии (тему, аудиторию, окна, состояние). "
                    + "Ростер группы намеренно не отдаётся: сначала студент подтверждает код аудитории, "
                    + "затем ищет себя по фамилии через POST /{id}/students.")
    @GetMapping("/{id}")
    public ResponseEntity<PublicCheckInSessionResponse> getPublicCheckInSession(
            @Parameter(description = "ID check-in сессии") @PathVariable UUID id) {
        return ResponseEntity.ok(checkInSessionApi.getPublic(id));
    }

    @Operation(
            summary = "Подтвердить код аудитории",
            description = "Шаг перед поиском: проверяет код аудитории. 204 — код верный (можно искать), "
                    + "400 — код неверный либо сессия закрыта.")
    @PostMapping("/{id}/verify-code")
    public ResponseEntity<Void> verifyCheckInCode(
            @Parameter(description = "ID check-in сессии") @PathVariable UUID id,
            @Valid @RequestBody VerifyCheckInCodeRequest request) {
        checkInSessionApi.verifyCode(id, request.code());
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Найти себя по фамилии (за кодом аудитории)",
            description = "Доступ к списку — только за кодом аудитории: в теле передаются code и query. "
                    + "Возвращает только совпадения с маскированным ФИО и id (id нужен для отметки), без статусов "
                    + "посещаемости. Неверный код — 400; пустой/короткий запрос — пустой список.")
    @PostMapping("/{id}/students")
    public ResponseEntity<List<PublicStudentResponse>> searchPublicCheckInStudents(
            @Parameter(description = "ID check-in сессии") @PathVariable UUID id,
            @Valid @RequestBody PublicStudentSearchRequest request) {
        return ResponseEntity.ok(checkInSessionApi.searchStudents(id, request.code(), request.query()));
    }

    @Operation(
            summary = "Отметиться студенту",
            description = "Требует ID студента (из результата поиска) и код аудитории, показанный преподавателем. "
                    + "Возвращает только собственный статус отметки.")
    @PostMapping("/{id}/check-in")
    public ResponseEntity<PublicCheckInRecordResponse> submitCheckIn(
            @Parameter(description = "ID check-in сессии") @PathVariable UUID id,
            @Valid @RequestBody StudentCheckInRequest request) {
        return ResponseEntity.ok(checkInRecordsApi.checkIn(id, request));
    }
}
