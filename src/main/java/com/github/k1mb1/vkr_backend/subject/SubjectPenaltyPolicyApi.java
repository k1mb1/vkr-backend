package com.github.k1mb1.vkr_backend.subject;

import com.github.k1mb1.vkr_backend.subject.web.requests.PenaltyPolicyRequest;
import com.github.k1mb1.vkr_backend.subject.web.responses.PenaltyPolicyResponse;
import jakarta.validation.Valid;

import java.util.UUID;

public interface SubjectPenaltyPolicyApi {
    PenaltyPolicyResponse getPenaltyPolicy(UUID subjectId);

    PenaltyPolicyResponse updatePenaltyPolicy(UUID subjectId, @Valid PenaltyPolicyRequest request);
}
