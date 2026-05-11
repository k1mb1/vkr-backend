package com.github.k1mb1.vkr_backend.subject.internal;

import com.github.k1mb1.vkr_backend.subject.SubjectsApi;
import com.github.k1mb1.vkr_backend.subject.domain.Subject;
import com.github.k1mb1.vkr_backend.subject.web.filters.SubjectFilter;
import com.github.k1mb1.vkr_backend.subject.web.requests.CreateSubjectRequest;
import com.github.k1mb1.vkr_backend.subject.web.requests.UpdateSubjectRequest;
import com.github.k1mb1.vkr_backend.subject.web.responses.SubjectPageResponse;
import com.github.k1mb1.vkr_backend.subject.web.responses.SubjectResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class SubjectService implements SubjectsApi {

    final SubjectRepository subjectRepository;

    final SubjectMapper subjectMapper;

    @Transactional
    @Override
    public SubjectResponse update(UUID id, UpdateSubjectRequest request) {
        var subject = subjectRepository
            .findById(id)
            .orElseThrow(() ->
                new jakarta.persistence.EntityNotFoundException(
                    "Subject not found: " + id
                )
            );

        subjectMapper.updateEntity(request, subject);
        return subjectMapper.toFullResponse(subjectRepository.save(subject));
    }

    @Override
    public Page<SubjectPageResponse> getPage(
        SubjectFilter filter,
        Pageable pageable
    ) {
        return subjectRepository
            .findAll(new SubjectSpecifications(filter).toSpec(), pageable)
            .map(subjectMapper::toResponse);
    }

    @Transactional
    @Override
    public SubjectResponse create(CreateSubjectRequest request) {
        var subject = Subject.builder()
                .name(request.name())
                .description(request.description())
                .build();
        return subjectMapper.toFullResponse(subjectRepository.save(subject));
    }
}
