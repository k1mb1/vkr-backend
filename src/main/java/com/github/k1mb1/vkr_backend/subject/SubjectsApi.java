package com.github.k1mb1.vkr_backend.subject;

import com.github.k1mb1.vkr_backend.subject.web.requests.CreateSubjectRequest;
import com.github.k1mb1.vkr_backend.subject.web.requests.UpdateSubjectRequest;
import com.github.k1mb1.vkr_backend.subject.web.responses.SubjectResponse;
import jakarta.validation.Valid;

import java.util.UUID;

public interface SubjectsApi {
    SubjectResponse updateSubject(UUID id, UpdateSubjectRequest request);

    SubjectResponse createSubject(
        @Valid CreateSubjectRequest request
    );
}
