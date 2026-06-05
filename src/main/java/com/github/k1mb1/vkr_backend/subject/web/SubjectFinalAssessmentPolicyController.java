package com.github.k1mb1.vkr_backend.subject.web;

import com.github.k1mb1.vkr_backend.subject.SubjectFinalAssessmentPolicyApi;
import com.github.k1mb1.vkr_backend.subject.web.requests.FinalAssessmentPolicyRequest;
import com.github.k1mb1.vkr_backend.subject.web.responses.FinalAssessmentPolicyResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/final-assessment-policy/subjects/{subjectId}")
class SubjectFinalAssessmentPolicyController {

    final SubjectFinalAssessmentPolicyApi api;

    @GetMapping
    FinalAssessmentPolicyResponse getFinalAssessmentPolicy(@PathVariable UUID subjectId) {
        return api.getFinalAssessmentPolicy(subjectId);
    }

    @PutMapping
    FinalAssessmentPolicyResponse updateFinalAssessmentPolicy(
        @PathVariable UUID subjectId,
        @Valid @RequestBody FinalAssessmentPolicyRequest request
    ) {
        return api.updateFinalAssessmentPolicy(subjectId, request);
    }
}
