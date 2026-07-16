package com.github.k1mb1.vkr_backend.subject.controller;

import com.github.k1mb1.vkr_backend.subject.api.GradingHighlightPolicyResponse;
import com.github.k1mb1.vkr_backend.subject.service.SubjectGradingHighlightPolicyService;
import com.github.k1mb1.vkr_backend.subject.service.dto.request.GradingHighlightPolicyRequest;
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
        value = "/api/grading-highlight-policy/subjects/{subjectId}",
        produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Subject grading highlight policy", description = "Цветовая подсветка ячеек оценок (расчёт на фронте)")
@RestController
@RequiredArgsConstructor
public class SubjectGradingHighlightPolicyController {

    final SubjectGradingHighlightPolicyService gradingHighlightPolicyApi;

    @Operation(summary = "Получить политику подсветки оценок предмета")
    @GetMapping
    public ResponseEntity<GradingHighlightPolicyResponse> getGradingHighlightPolicy(
            @Parameter(description = "ID предмета") @PathVariable UUID subjectId) {
        return ResponseEntity.ok(gradingHighlightPolicyApi.getGradingHighlightPolicy(subjectId));
    }

    @Operation(
            summary = "Задать/обновить политику подсветки оценок",
            description = "enabled=false выключает подсветку. При enabled=true цвета применяются на фронте.")
    @PutMapping
    public ResponseEntity<GradingHighlightPolicyResponse> updateGradingHighlightPolicy(
            @Parameter(description = "ID предмета") @PathVariable UUID subjectId,
            @Valid @RequestBody
                    @io.swagger.v3.oas.annotations.parameters.RequestBody(
                            description = "Цвета подсветки оценок",
                            required = true)
                    GradingHighlightPolicyRequest request) {
        return ResponseEntity.ok(gradingHighlightPolicyApi.updateGradingHighlightPolicy(subjectId, request));
    }
}
