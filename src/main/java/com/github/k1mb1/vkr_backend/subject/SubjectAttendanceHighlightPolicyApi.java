package com.github.k1mb1.vkr_backend.subject;

import com.github.k1mb1.vkr_backend.subject.web.requests.AttendanceHighlightPolicyRequest;
import com.github.k1mb1.vkr_backend.subject.web.responses.AttendanceHighlightPolicyResponse;
import jakarta.validation.Valid;

import java.util.UUID;

public interface SubjectAttendanceHighlightPolicyApi {

    AttendanceHighlightPolicyResponse getAttendanceHighlightPolicy(UUID subjectId);

    AttendanceHighlightPolicyResponse updateAttendanceHighlightPolicy(
        UUID subjectId,
        @Valid AttendanceHighlightPolicyRequest request
    );
}
