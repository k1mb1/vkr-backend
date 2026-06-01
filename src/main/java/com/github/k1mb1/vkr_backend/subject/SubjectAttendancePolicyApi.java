package com.github.k1mb1.vkr_backend.subject;

import com.github.k1mb1.vkr_backend.subject.web.requests.AttendancePolicyRequest;
import com.github.k1mb1.vkr_backend.subject.web.responses.AttendancePolicyResponse;
import jakarta.validation.Valid;

import java.util.UUID;

public interface SubjectAttendancePolicyApi {
    AttendancePolicyResponse getAttendancePolicy(UUID subjectId);

    AttendancePolicyResponse updateAttendancePolicy(UUID subjectId, @Valid AttendancePolicyRequest request);
}
