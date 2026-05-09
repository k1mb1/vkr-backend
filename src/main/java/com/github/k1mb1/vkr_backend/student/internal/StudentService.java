package com.github.k1mb1.vkr_backend.student.internal;

import com.github.k1mb1.vkr_backend.group.GroupReferenceService;
import com.github.k1mb1.vkr_backend.group.domain.Group;
import com.github.k1mb1.vkr_backend.group.domain.Subgroup;
import com.github.k1mb1.vkr_backend.student.StudentApi;
import com.github.k1mb1.vkr_backend.student.domain.Student;
import com.github.k1mb1.vkr_backend.student.internal.web.response.StudentResponse;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class StudentService implements StudentApi {

    final StudentRepository studentRepository;
    final GroupReferenceService groupReferenceService;

    final StudentMapper studentMapper;

    public List<StudentResponse> findActiveByGroup(UUID groupId) {
        return studentRepository
            .findByGroupIdAndArchivedAtIsNull(groupId)
            .stream()
            .map(studentMapper::toResponse)
            .toList();
    }

    public List<StudentResponse> findByGroup(UUID groupId) {
        return studentRepository
            .findByGroupId(groupId)
            .stream()
            .map(studentMapper::toResponse)
            .toList();
    }

    @Transactional
    public void archive(UUID studentId) {
        var student = studentRepository
            .findById(studentId)
            .orElseThrow(() ->
                new jakarta.persistence.EntityNotFoundException(
                    "Student not found: " + studentId
                )
            );
        student.archive();
    }

    @Transactional
    public void update(UUID studentId, String username, UUID subgroupId) {
        var student = studentRepository
            .findById(studentId)
            .orElseThrow(() ->
                new jakarta.persistence.EntityNotFoundException(
                    "Student not found: " + studentId
                )
            );
        var subgroup =
            subgroupId != null
                ? groupReferenceService.getSubgroupReferenceById(subgroupId)
                : null;
        student.setUsername(username);
        student.setSubgroup(subgroup);
        if (student.isArchived()) {
            student.unarchive();
        }
    }

    @Transactional
    public StudentResponse create(
        String username,
        UUID groupId,
        UUID subgroupId
    ) {
        var group = groupReferenceService.getGroupReferenceById(groupId);
        var subgroup =
            subgroupId != null
                ? groupReferenceService.getSubgroupReferenceById(subgroupId)
                : null;
        var student = Student.builder()
            .username(username)
            .group(group)
            .subgroup(subgroup)
            .build();
        return studentMapper.toResponse(studentRepository.save(student));
    }

    @Transactional
    public void deleteByGroup(UUID groupId) {
        studentRepository.deleteByGroupId(groupId);
    }
}
