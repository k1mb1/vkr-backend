package com.github.k1mb1.vkr_backend.subject;

import com.github.k1mb1.vkr_backend.subject.domain.SubjectEntity;
import java.util.UUID;

public interface SubjectReferenceService {
    SubjectEntity getSubjectReferenceById(UUID id);
}
