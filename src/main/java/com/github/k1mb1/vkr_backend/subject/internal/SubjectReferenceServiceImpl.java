package com.github.k1mb1.vkr_backend.subject.internal;

import com.github.k1mb1.vkr_backend.subject.SubjectReferenceService;
import com.github.k1mb1.vkr_backend.subject.domain.Subject;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
class SubjectReferenceServiceImpl implements SubjectReferenceService {

    final SubjectRepository subjectRepository;

    @Override
    public Subject getSubjectReferenceById(UUID id) {
        return subjectRepository.getReferenceById(id);
    }
}
