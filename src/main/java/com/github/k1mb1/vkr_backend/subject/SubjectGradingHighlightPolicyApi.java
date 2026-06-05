package com.github.k1mb1.vkr_backend.subject;

import com.github.k1mb1.vkr_backend.subject.web.requests.GradingHighlightPolicyRequest;
import com.github.k1mb1.vkr_backend.subject.web.responses.GradingHighlightPolicyResponse;
import jakarta.validation.Valid;

import java.util.UUID;

public interface SubjectGradingHighlightPolicyApi {

    GradingHighlightPolicyResponse getGradingHighlightPolicy(UUID subjectId);

    GradingHighlightPolicyResponse updateGradingHighlightPolicy(
        UUID subjectId,
        @Valid GradingHighlightPolicyRequest request
    );
}
