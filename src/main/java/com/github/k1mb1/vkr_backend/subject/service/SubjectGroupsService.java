package com.github.k1mb1.vkr_backend.subject.service;

import com.github.k1mb1.vkr_backend.common.exception.ConflictException;
import com.github.k1mb1.vkr_backend.common.exception.ResourceNotFoundException;
import com.github.k1mb1.vkr_backend.group.api.GroupSubjectsPort;
import com.github.k1mb1.vkr_backend.subject.domain.SubjectEntity;
import com.github.k1mb1.vkr_backend.subject.repository.SubjectGroupRefRepository;
import com.github.k1mb1.vkr_backend.subject.repository.SubjectRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Реализация {@link GroupSubjectsPort}: связь «предмет — группы» принадлежит
 * агрегату Subject, поэтому мутации живут здесь, а модуль group лишь вызывает порт.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubjectGroupsService implements GroupSubjectsPort {

    private final SubjectRepository subjectRepository;
    private final SubjectGroupRefRepository groupRefRepository;

    @Override
    public List<UUID> groupIdsOfSubject(UUID subjectId) {
        return subjectRepository.findGroupIdsById(subjectId);
    }

    @Transactional
    @Override
    public void attachGroup(UUID groupId, UUID subjectId) {
        var subject = loadSubject(subjectId);
        if (!subject.getGroups().add(groupRefRepository.getReferenceById(groupId))) {
            throw new ConflictException("Group " + groupId + " is already attached to subject " + subjectId);
        }
        subjectRepository.save(subject);
    }

    @Transactional
    @Override
    public void detachGroup(UUID groupId, UUID subjectId) {
        var subject = loadSubject(subjectId);
        var removed = subject.getGroups().removeIf(g -> g.getId().equals(groupId));
        if (!removed) {
            throw new jakarta.persistence.EntityNotFoundException(
                    "Group " + groupId + " is not attached to subject " + subjectId);
        }
        subjectRepository.save(subject);
    }

    private SubjectEntity loadSubject(UUID subjectId) {
        return subjectRepository
                .findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject", subjectId));
    }
}
