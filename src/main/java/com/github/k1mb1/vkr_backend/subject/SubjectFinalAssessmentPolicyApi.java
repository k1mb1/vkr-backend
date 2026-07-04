package com.github.k1mb1.vkr_backend.subject;

import com.github.k1mb1.vkr_backend.subject.web.requests.FinalAssessmentPolicyRequest;
import com.github.k1mb1.vkr_backend.subject.web.responses.FinalAssessmentPolicyResponse;
import jakarta.validation.Valid;
import java.util.UUID;

public interface SubjectFinalAssessmentPolicyApi {

    FinalAssessmentPolicyResponse getFinalAssessmentPolicy(UUID subjectId);

    FinalAssessmentPolicyResponse updateFinalAssessmentPolicy(
            UUID subjectId, @Valid FinalAssessmentPolicyRequest request);
}
