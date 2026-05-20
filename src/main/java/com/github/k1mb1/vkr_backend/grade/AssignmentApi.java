package com.github.k1mb1.vkr_backend.grade;

import com.github.k1mb1.vkr_backend.grade.web.requests.CreateAssignmentRequest;
import com.github.k1mb1.vkr_backend.grade.web.requests.UpdateAssignmentRequest;
import com.github.k1mb1.vkr_backend.grade.web.responses.AssignmentResponse;

import java.util.UUID;

public interface AssignmentApi {
    AssignmentResponse create(CreateAssignmentRequest request);

    AssignmentResponse findById(UUID id);

    AssignmentResponse update(UUID id, UpdateAssignmentRequest request);

    void delete(UUID id);
}
