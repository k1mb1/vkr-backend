package com.github.k1mb1.vkr_backend.subject;

import com.github.k1mb1.vkr_backend.subject.web.requests.CreateSubjectAssignmentRequest;
import com.github.k1mb1.vkr_backend.subject.web.requests.UpdateSubjectAssignmentRequest;
import com.github.k1mb1.vkr_backend.subject.web.responses.SubjectAssignmentResponse;

import java.util.UUID;

public interface SubjectAssignmentApi {
    SubjectAssignmentResponse create(CreateSubjectAssignmentRequest request);

    SubjectAssignmentResponse update(UUID id, UpdateSubjectAssignmentRequest request);

    void delete(UUID id);
}
