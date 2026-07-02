package com.github.k1mb1.vkr_backend.subject.web;

import com.github.k1mb1.vkr_backend.subject.SubjectFinalAssessmentPolicyApi;
import com.github.k1mb1.vkr_backend.subject.web.requests.FinalAssessmentPolicyRequest;
import com.github.k1mb1.vkr_backend.subject.web.responses.FinalAssessmentPolicyResponse;
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
    value = "/api/final-assessment-policy/subjects/{subjectId}",
    produces = MediaType.APPLICATION_JSON_VALUE
)
@Tag(
    name = "Subject final assessment policy",
    description = "Промежуточная аттестация: банды и режим посещаемости (расчёт на фронте)"
)
@RestController
@RequiredArgsConstructor
public class SubjectFinalAssessmentPolicyController {

    final SubjectFinalAssessmentPolicyApi finalAssessmentPolicyApi;

    @Operation(summary = "Получить политику промежуточной аттестации предмета")
    @GetMapping
    public ResponseEntity<FinalAssessmentPolicyResponse> getFinalAssessmentPolicy(
        @Parameter(description = "ID предмета")
        @PathVariable UUID subjectId
    ) {
        return ResponseEntity.ok(finalAssessmentPolicyApi.getFinalAssessmentPolicy(subjectId));
    }

    @Operation(
        summary = "Задать/обновить политику промежуточной аттестации",
        description = "enabled=false выключает итоги. При enabled=true обязателен непустой список банд."
    )
    @PutMapping
    public ResponseEntity<FinalAssessmentPolicyResponse> updateFinalAssessmentPolicy(
        @Parameter(description = "ID предмета")
        @PathVariable UUID subjectId,
        @Valid
        @RequestBody
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Параметры промежуточной аттестации", required = true
        )
        FinalAssessmentPolicyRequest request
    ) {
        return ResponseEntity.ok(
            finalAssessmentPolicyApi.updateFinalAssessmentPolicy(subjectId, request)
        );
    }
}
