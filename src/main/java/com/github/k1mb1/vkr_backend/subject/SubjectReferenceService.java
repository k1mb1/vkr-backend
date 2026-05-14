package com.github.k1mb1.vkr_backend.subject;

import com.github.k1mb1.vkr_backend.subject.domain.Subject;

import java.util.UUID;

public interface SubjectReferenceService {
    Subject getSubjectReferenceById(UUID id);
}
