package com.github.k1mb1.vkr_backend.education.subjects.internal;

import static com.github.k1mb1.vkr_backend.shared.web.ErrorMessages.NOT_FOUND_MESSAGE;

import com.github.k1mb1.vkr_backend.education.structure.api.StructureQueryFacade;
import com.github.k1mb1.vkr_backend.education.subjects.api.requests.CreateSubjectRequest;
import com.github.k1mb1.vkr_backend.education.subjects.api.requests.UpdateSubjectRequest;
import com.github.k1mb1.vkr_backend.education.subjects.api.responses.GroupAttachmentResponse;
import com.github.k1mb1.vkr_backend.education.subjects.api.responses.SubjectResponse;
import jakarta.persistence.EntityNotFoundException;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubjectService {
    final SubjectRepository subjectRepository;
    final SubjectMapper subjectMapper;
    final StructureQueryFacade structureQueryFacade;

    public List<SubjectResponse> findAll(Specification<SubjectEntity> spec) {
        return subjectRepository.findAll(spec).stream().map(subjectMapper::toResponse).toList();
    }

    public SubjectEntity getReferenceById(UUID id) {
        return subjectRepository.getReferenceById(id);
    }

    @Transactional
    public SubjectResponse create(CreateSubjectRequest request) {
        var entity = subjectMapper.toEntity(request);
        entity.getTeacherIds().add(request.teacherId());
        return subjectMapper.toResponse(subjectRepository.save(entity));
    }

    @Transactional
    public SubjectResponse update(UUID id, UpdateSubjectRequest request) {
        var entity = subjectRepository.getReferenceById(id);
        subjectMapper.update(entity, request);
        return subjectMapper.toResponse(subjectRepository.save(entity));
    }

    @Transactional
    public void remove(UUID id) {
        var entity = subjectRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(NOT_FOUND_MESSAGE.formatted("Subject", id)));
        subjectRepository.delete(entity);
    }

    @Transactional
    public GroupAttachmentResponse attachGroup(UUID subjectId, UUID groupId) {
        var subject = subjectRepository.findById(subjectId)
            .orElseThrow(() -> new EntityNotFoundException(NOT_FOUND_MESSAGE.formatted("Subject", subjectId)));

        if (!structureQueryFacade.groupExists(groupId))
            throw new EntityNotFoundException(NOT_FOUND_MESSAGE.formatted("Main group", groupId));

        String groupName = structureQueryFacade.getGroupNameById(groupId);
        List<UUID> newStudentIds = structureQueryFacade.findAllStudentIdsByGroupId(groupId);
        var existing = new HashSet<>(subject.getStudentIds());
        newStudentIds.stream().filter(sid -> !existing.contains(sid)).forEach(subject.getStudentIds()::add);

        var saved = subjectRepository.save(subject);
        return new GroupAttachmentResponse(saved.getId(), saved.getName(), groupId, groupName);
    }
}
