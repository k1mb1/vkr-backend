package com.github.k1mb1.vkr_backend.subject.web;

import com.github.k1mb1.vkr_backend.subject.SubjectAttendancePolicyApi;
import com.github.k1mb1.vkr_backend.subject.web.requests.AttendancePolicyRequest;
import com.github.k1mb1.vkr_backend.subject.web.responses.AttendancePolicyResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping(value = "/api/attendance-policy/subjects/{subjectId}", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Subject attendance policy", description = "Связка посещаемости с баллом (расчёт на фронте)")
@RestController
@RequiredArgsConstructor
public class SubjectAttendancePolicyController {

    final SubjectAttendancePolicyApi attendancePolicyApi;

    @Operation(summary = "Получить связку посещаемости с баллом предмета")
    @GetMapping
    public ResponseEntity<AttendancePolicyResponse> getAttendancePolicy(
            @Parameter(description = "ID предмета") @PathVariable UUID subjectId) {
        return ResponseEntity.ok(attendancePolicyApi.getAttendancePolicy(subjectId));
    }

    @Operation(
            summary = "Задать/обновить связку посещаемости с баллом",
            description = "enabled=false выключает фичу. При enabled=true обязательны все коэффициенты.")
    @PutMapping
    public ResponseEntity<AttendancePolicyResponse> updateAttendancePolicy(
            @Parameter(description = "ID предмета") @PathVariable UUID subjectId,
            @Valid @RequestBody
                    @io.swagger.v3.oas.annotations.parameters.RequestBody(
                            description = "Коэффициенты посещаемости",
                            required = true)
                    AttendancePolicyRequest request) {
        return ResponseEntity.ok(attendancePolicyApi.updateAttendancePolicy(subjectId, request));
    }
}
