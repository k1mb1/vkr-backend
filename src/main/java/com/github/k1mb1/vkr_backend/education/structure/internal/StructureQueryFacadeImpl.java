package com.github.k1mb1.vkr_backend.education.structure.internal;

import com.github.k1mb1.vkr_backend.education.structure.api.StructureQueryFacade;
import com.github.k1mb1.vkr_backend.education.structure.api.StudentSummary;
import jakarta.persistence.EntityNotFoundException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class StructureQueryFacadeImpl implements StructureQueryFacade {
    final StudentRepository studentRepository;
    final TeacherRepository teacherRepository;
    final StudentGroupRepository groupRepository;

    @Override
    public boolean studentExists(UUID id) {
        return studentRepository.existsById(id);
    }

    @Override
    public boolean teacherExists(UUID id) {
        return teacherRepository.existsById(id);
    }

    @Override
    public boolean groupExists(UUID id) {
        return groupRepository.existsById(id);
    }

    @Override
    public List<StudentSummary> findStudentsByIds(Collection<UUID> ids) {
        if (ids.isEmpty()) return List.of();
        return studentRepository.findAllByIdIn(ids).stream()
            .map(s -> new StudentSummary(s.getId(), s.getUsername()))
            .toList();
    }

    @Override
    public List<UUID> findAllStudentIdsByGroupId(UUID groupId) {
        var group = groupRepository.findWithSubgroupsAndStudentsById(groupId)
            .orElseThrow(() -> new EntityNotFoundException("Group not found: " + groupId));
        var ids = new java.util.ArrayList<UUID>();
        group.getStudents().forEach(s -> ids.add(s.getId()));
        group.getSubgroups().forEach(sg -> sg.getStudents().forEach(s -> ids.add(s.getId())));
        return ids;
    }

    @Override
    public String getGroupNameById(UUID groupId) {
        return groupRepository.findById(groupId)
            .map(StudentGroupEntity::getName)
            .orElseThrow(() -> new EntityNotFoundException("Group not found: " + groupId));
    }
}
