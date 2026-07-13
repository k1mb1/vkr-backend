package com.github.k1mb1.vkr_backend.subject.service;

import com.github.k1mb1.vkr_backend.subject.SubjectReferenceService;
import com.github.k1mb1.vkr_backend.subject.domain.SubjectEntity;
import com.github.k1mb1.vkr_backend.subject.repository.SubjectRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SubjectReferenceServiceImpl implements SubjectReferenceService {

    final SubjectRepository subjectRepository;

    @Override
    public SubjectEntity getSubjectReferenceById(UUID id) {
        return subjectRepository.getReferenceById(id);
    }
}
