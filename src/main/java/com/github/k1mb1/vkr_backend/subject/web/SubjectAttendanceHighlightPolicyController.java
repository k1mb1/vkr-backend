package com.github.k1mb1.vkr_backend.subject.web;

import com.github.k1mb1.vkr_backend.subject.SubjectAttendanceHighlightPolicyApi;
import com.github.k1mb1.vkr_backend.subject.web.requests.AttendanceHighlightPolicyRequest;
import com.github.k1mb1.vkr_backend.subject.web.responses.AttendanceHighlightPolicyResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequestMapping(
    value = "/api/attendance-highlight-policy/subjects/{subjectId}",
    produces = MediaType.APPLICATION_JSON_VALUE
)
@Tag(
    name = "Subject attendance highlight policy",
    description = "Цветовая подсветка ячеек посещаемости (расчёт на фронте)"
)
@RestController
@RequiredArgsConstructor
public class SubjectAttendanceHighlightPolicyController {

    final SubjectAttendanceHighlightPolicyApi attendanceHighlightPolicyApi;

    @Operation(summary = "Получить политику подсветки посещаемости предмета")
    @GetMapping
    public ResponseEntity<AttendanceHighlightPolicyResponse> getAttendanceHighlightPolicy(
        @Parameter(description = "ID предмета")
        @PathVariable UUID subjectId
    ) {
        return ResponseEntity.ok(attendanceHighlightPolicyApi.getAttendanceHighlightPolicy(subjectId));
    }

    @Operation(
        summary = "Задать/обновить политику подсветки посещаемости",
        description = "enabled=false выключает подсветку. При enabled=true цвета применяются на фронте."
    )
    @PutMapping
    public ResponseEntity<AttendanceHighlightPolicyResponse> updateAttendanceHighlightPolicy(
        @Parameter(description = "ID предмета")
        @PathVariable UUID subjectId,
        @Valid
        @RequestBody
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Цвета подсветки посещаемости", required = true
        )
        AttendanceHighlightPolicyRequest request
    ) {
        return ResponseEntity.ok(
            attendanceHighlightPolicyApi.updateAttendanceHighlightPolicy(subjectId, request)
        );
    }
}
