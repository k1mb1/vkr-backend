package com.github.k1mb1.vkr_backend.subject.web;

import com.github.k1mb1.vkr_backend.subject.SubjectGradingHighlightPolicyApi;
import com.github.k1mb1.vkr_backend.subject.web.requests.GradingHighlightPolicyRequest;
import com.github.k1mb1.vkr_backend.subject.web.responses.GradingHighlightPolicyResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/grading-highlight-policy/subjects/{subjectId}")
class SubjectGradingHighlightPolicyController {

    final SubjectGradingHighlightPolicyApi api;

    @GetMapping
    GradingHighlightPolicyResponse getGradingHighlightPolicy(@PathVariable UUID subjectId) {
        return api.getGradingHighlightPolicy(subjectId);
    }

    @PutMapping
    GradingHighlightPolicyResponse updateGradingHighlightPolicy(
        @PathVariable UUID subjectId,
        @Valid @RequestBody GradingHighlightPolicyRequest request
    ) {
        return api.updateGradingHighlightPolicy(subjectId, request);
    }
}
