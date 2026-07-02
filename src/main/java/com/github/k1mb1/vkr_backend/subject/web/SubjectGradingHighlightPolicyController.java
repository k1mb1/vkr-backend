package com.github.k1mb1.vkr_backend.subject.web;

import com.github.k1mb1.vkr_backend.subject.SubjectGradingHighlightPolicyApi;
import com.github.k1mb1.vkr_backend.subject.web.requests.GradingHighlightPolicyRequest;
import com.github.k1mb1.vkr_backend.subject.web.responses.GradingHighlightPolicyResponse;
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
    value = "/api/grading-highlight-policy/subjects/{subjectId}",
    produces = MediaType.APPLICATION_JSON_VALUE
)
@Tag(
    name = "Subject grading highlight policy",
    description = "Цветовая подсветка ячеек оценок (расчёт на фронте)"
)
@RestController
@RequiredArgsConstructor
public class SubjectGradingHighlightPolicyController {

    final SubjectGradingHighlightPolicyApi gradingHighlightPolicyApi;

    @Operation(summary = "Получить политику подсветки оценок предмета")
    @GetMapping
    public ResponseEntity<GradingHighlightPolicyResponse> getGradingHighlightPolicy(
        @Parameter(description = "ID предмета")
        @PathVariable UUID subjectId
    ) {
        return ResponseEntity.ok(gradingHighlightPolicyApi.getGradingHighlightPolicy(subjectId));
    }

    @Operation(
        summary = "Задать/обновить политику подсветки оценок",
        description = "enabled=false выключает подсветку. При enabled=true цвета применяются на фронте."
    )
    @PutMapping
    public ResponseEntity<GradingHighlightPolicyResponse> updateGradingHighlightPolicy(
        @Parameter(description = "ID предмета")
        @PathVariable UUID subjectId,
        @Valid
        @RequestBody
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Цвета подсветки оценок", required = true
        )
        GradingHighlightPolicyRequest request
    ) {
        return ResponseEntity.ok(
            gradingHighlightPolicyApi.updateGradingHighlightPolicy(subjectId, request)
        );
    }
}
