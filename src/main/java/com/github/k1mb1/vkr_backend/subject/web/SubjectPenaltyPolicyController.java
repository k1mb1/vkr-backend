package com.github.k1mb1.vkr_backend.subject.web;

import com.github.k1mb1.vkr_backend.subject.SubjectPenaltyPolicyApi;
import com.github.k1mb1.vkr_backend.subject.web.requests.PenaltyPolicyRequest;
import com.github.k1mb1.vkr_backend.subject.web.responses.PenaltyPolicyResponse;
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

@RequestMapping(value = "/api/penalty-policy/subjects/{subjectId}", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Subject penalty policy", description = "Политика понижения балла за просрочку (расчёт на фронте)")
@RestController
@RequiredArgsConstructor
public class SubjectPenaltyPolicyController {

    final SubjectPenaltyPolicyApi penaltyPolicyApi;

    @Operation(summary = "Получить политику понижения балла предмета")
    @GetMapping
    public ResponseEntity<PenaltyPolicyResponse> getPenaltyPolicy(
            @Parameter(description = "ID предмета") @PathVariable UUID subjectId) {
        return ResponseEntity.ok(penaltyPolicyApi.getPenaltyPolicy(subjectId));
    }

    @Operation(
            summary = "Задать/обновить политику понижения балла предмета",
            description = "enabled=false выключает фичу. При enabled=true обязательны все параметры.")
    @PutMapping
    public ResponseEntity<PenaltyPolicyResponse> updatePenaltyPolicy(
            @Parameter(description = "ID предмета") @PathVariable UUID subjectId,
            @Valid @RequestBody
                    @io.swagger.v3.oas.annotations.parameters.RequestBody(
                            description = "Параметры политики понижения",
                            required = true)
                    PenaltyPolicyRequest request) {
        return ResponseEntity.ok(penaltyPolicyApi.updatePenaltyPolicy(subjectId, request));
    }
}
