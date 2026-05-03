package com.github.k1mb1.vkr_backend.education.subjects.internal;

import com.github.k1mb1.vkr_backend.education.subjects.api.SubjectQueryFacade;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class SubjectQueryFacadeImpl implements SubjectQueryFacade {
    final SubjectRepository subjectRepository;

    @Override
    public boolean existsById(UUID id) {
        return subjectRepository.existsById(id);
    }

    @Override
    public String getNameById(UUID id) {
        return subjectRepository.findById(id)
            .map(SubjectEntity::getName)
            .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Subject not found: " + id));
    }

    @Override
    public List<UUID> findStudentIdsBySubjectId(UUID subjectId) {
        return subjectRepository.findById(subjectId)
            .map(s -> s.getStudentIds().stream().toList())
            .orElse(List.of());
    }
}
