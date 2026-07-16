package com.github.k1mb1.vkr_backend.subject.controller;

import com.github.k1mb1.vkr_backend.subject.api.FinalAssessmentPolicyResponse;
import com.github.k1mb1.vkr_backend.subject.service.SubjectFinalAssessmentPolicyService;
import com.github.k1mb1.vkr_backend.subject.service.dto.request.FinalAssessmentPolicyRequest;
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

@RequestMapping(
        value = "/api/final-assessment-policy/subjects/{subjectId}",
        produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(
        name = "Subject final assessment policy",
        description = "Промежуточная аттестация: банды и режим посещаемости (расчёт на фронте)")
@RestController
@RequiredArgsConstructor
public class SubjectFinalAssessmentPolicyController {

    final SubjectFinalAssessmentPolicyService finalAssessmentPolicyApi;

    @Operation(summary = "Получить политику промежуточной аттестации предмета")
    @GetMapping
    public ResponseEntity<FinalAssessmentPolicyResponse> getFinalAssessmentPolicy(
            @Parameter(description = "ID предмета") @PathVariable UUID subjectId) {
        return ResponseEntity.ok(finalAssessmentPolicyApi.getFinalAssessmentPolicy(subjectId));
    }

    @Operation(
            summary = "Задать/обновить политику промежуточной аттестации",
            description = "enabled=false выключает итоги. При enabled=true обязателен непустой список банд.")
    @PutMapping
    public ResponseEntity<FinalAssessmentPolicyResponse> updateFinalAssessmentPolicy(
            @Parameter(description = "ID предмета") @PathVariable UUID subjectId,
            @Valid @RequestBody
                    @io.swagger.v3.oas.annotations.parameters.RequestBody(
                            description = "Параметры промежуточной аттестации",
                            required = true)
                    FinalAssessmentPolicyRequest request) {
        return ResponseEntity.ok(finalAssessmentPolicyApi.updateFinalAssessmentPolicy(subjectId, request));
    }
}
