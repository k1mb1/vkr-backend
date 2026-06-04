package com.github.k1mb1.vkr_backend.subject.web;

import com.github.k1mb1.vkr_backend.subject.SubjectCheckInPolicyApi;
import com.github.k1mb1.vkr_backend.subject.web.requests.CheckInPolicyRequest;
import com.github.k1mb1.vkr_backend.subject.web.responses.CheckInPolicyResponse;
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
    value = "/api/check-in-policy/subjects/{subjectId}", produces = MediaType.APPLICATION_JSON_VALUE
)
@Tag(
    name = "Subject check-in policy",
    description = "Единое время на отметку (check-in) для всего предмета"
)
@RestController
@RequiredArgsConstructor
public class SubjectCheckInPolicyController {

    final SubjectCheckInPolicyApi checkInPolicyApi;

    @Operation(summary = "Получить политику окон check-in предмета")
    @GetMapping
    public ResponseEntity<CheckInPolicyResponse> getCheckInPolicy(
        @Parameter(description = "ID предмета")
        @PathVariable
        UUID subjectId
    ) {
        return ResponseEntity.ok(checkInPolicyApi.getCheckInPolicy(subjectId));
    }

    @Operation(
        summary = "Задать/обновить политику окон check-in",
        description = "enabled=false выключает фичу (окна задаются при запуске сессии). " +
            "При enabled=true окна обязательны и применяются ко всем сессиям предмета."
    )
    @PutMapping
    public ResponseEntity<CheckInPolicyResponse> updateCheckInPolicy(
        @Parameter(description = "ID предмета")
        @PathVariable
        UUID subjectId,
        @Valid
        @RequestBody
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Параметры окон check-in", required = true
        )
        CheckInPolicyRequest request
    ) {
        return ResponseEntity.ok(checkInPolicyApi.updateCheckInPolicy(subjectId, request));
    }
}
