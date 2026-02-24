package com.github.k1mb1.vkr_backend.domain.subjects;

import com.github.k1mb1.vkr_backend.domain.subjects.requests.CreateSubjectRequest;
import com.github.k1mb1.vkr_backend.domain.subjects.requests.UpdateSubjectRequest;
import com.github.k1mb1.vkr_backend.domain.subjects.responses.SubjectDetailsResponse;
import com.github.k1mb1.vkr_backend.domain.subjects.responses.SubjectResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final SubjectMapper subjectMapper;

    public Page<SubjectResponse> findAll(SubjectFilter filter, Pageable pageable) {
        return subjectRepository.findAll(filter.toSpecification(), pageable)
                .map(subjectMapper::toResponse);
    }

    public SubjectDetailsResponse findById(UUID id) {
        return subjectRepository.findWithDetailsById(id)
                .map(subjectMapper::toDetailsResponse)
                .orElseThrow(() -> new EntityNotFoundException("Subject not found: " + id));
    }

    public SubjectEntity findEntityById(UUID id) {
        return subjectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Subject not found: " + id));
    }

    @Transactional
    public SubjectResponse create(CreateSubjectRequest request) {
        var entity = subjectMapper.toEntity(request);
        return subjectMapper.toResponse(subjectRepository.save(entity));
    }

    @Transactional
    public SubjectResponse update(UUID id, UpdateSubjectRequest request) {
        var entity = subjectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Subject not found: " + id));
        subjectMapper.update(entity, request);
        return subjectMapper.toResponse(subjectRepository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        if (!subjectRepository.existsById(id)) {
            throw new EntityNotFoundException("Subject not found: " + id);
        }
        subjectRepository.deleteById(id);
    }

    @Transactional
    public void saveEntity(SubjectEntity entity) {
        subjectRepository.save(entity);
    }
}