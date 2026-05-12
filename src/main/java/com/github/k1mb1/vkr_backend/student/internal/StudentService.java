package com.github.k1mb1.vkr_backend.student.internal;

import com.github.k1mb1.vkr_backend.group.GroupReferenceService;
import com.github.k1mb1.vkr_backend.student.StudentApi;
import com.github.k1mb1.vkr_backend.student.domain.Student;
import com.github.k1mb1.vkr_backend.student.internal.web.requests.CreateStudentRequest;
import com.github.k1mb1.vkr_backend.student.internal.web.requests.UpdateStudentRequest;
import com.github.k1mb1.vkr_backend.student.internal.web.response.StudentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class StudentService
    implements StudentApi {

    final StudentRepository studentRepository;

    final GroupReferenceService groupReferenceService;

    final StudentMapper studentMapper;

    public List<StudentResponse> findActiveStudentsByGroup(UUID groupId) {
        return studentRepository.findByGroupIdAndArchivedAtIsNull(groupId)
            .stream()
            .map(studentMapper::toResponse)
            .toList();
    }

    public List<StudentResponse> findStudentsByGroup(UUID groupId) {
        return studentRepository.findByGroupId(groupId)
            .stream()
            .map(studentMapper::toResponse)
            .toList();
    }

    @Transactional
    public void archiveStudent(UUID studentId) {
        var student = studentRepository.findById(studentId)
            .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Student not found: " + studentId));
        student.archive();
    }

    @Transactional
    public void updateStudent(UUID studentId, UpdateStudentRequest request) {
        var student = studentRepository.findById(studentId)
            .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Student not found: " + studentId));
        studentMapper.updateEntity(request, student, groupReferenceService);
        studentRepository.save(student);
    }

    @Transactional
    public StudentResponse createStudent(CreateStudentRequest request) {
        var group = groupReferenceService.getGroupReferenceById(request.groupId());
        var subgroup = request.subgroupId() != null
                       ? groupReferenceService.getSubgroupReferenceById(request.subgroupId())
                       : null;
        var student = Student.builder()
            .username(request.username())
            .group(group)
            .subgroup(subgroup)
            .build();
        return studentMapper.toResponse(studentRepository.save(student));
    }

    @Transactional
    public void deleteStudentsByGroup(UUID groupId) {
        studentRepository.deleteByGroupId(groupId);
    }
}
