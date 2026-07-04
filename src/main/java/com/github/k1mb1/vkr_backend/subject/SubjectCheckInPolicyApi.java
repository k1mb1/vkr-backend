package com.github.k1mb1.vkr_backend.subject;

import com.github.k1mb1.vkr_backend.subject.web.requests.CheckInPolicyRequest;
import com.github.k1mb1.vkr_backend.subject.web.responses.CheckInPolicyResponse;
import jakarta.validation.Valid;
import java.util.UUID;

public interface SubjectCheckInPolicyApi {

    CheckInPolicyResponse getCheckInPolicy(UUID subjectId);

    CheckInPolicyResponse updateCheckInPolicy(UUID subjectId, @Valid CheckInPolicyRequest request);
}
