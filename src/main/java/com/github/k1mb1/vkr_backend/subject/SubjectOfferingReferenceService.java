package com.github.k1mb1.vkr_backend.subject;

import com.github.k1mb1.vkr_backend.subject.domain.SubjectOffering;

import java.util.List;
import java.util.UUID;

public interface SubjectOfferingReferenceService {

    List<SubjectOffering> findBySubjectId(UUID subjectId);
}
