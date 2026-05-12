package com.github.k1mb1.vkr_backend.subject.internal;

import com.github.k1mb1.vkr_backend.subject.SubjectOfferingReferenceService;
import com.github.k1mb1.vkr_backend.subject.domain.SubjectOffering;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
class SubjectOfferingReferenceServiceImpl
    implements SubjectOfferingReferenceService {

    final SubjectOfferingRepository subjectOfferingRepository;

    @Override
    public List<SubjectOffering> findBySubjectId(UUID subjectId) {
        return subjectOfferingRepository.findBySubjectId(subjectId);
    }
}
