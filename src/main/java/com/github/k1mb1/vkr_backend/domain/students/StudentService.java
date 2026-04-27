package com.github.k1mb1.vkr_backend.domain.students;

import static com.github.k1mb1.vkr_backend.apis.error.ErrorMessages.NOT_FOUND_MESSAGE;

import com.github.k1mb1.vkr_backend.domain.student_groups.StudentGroupRepository;
import com.github.k1mb1.vkr_backend.domain.students.requests.CreateStudentRequest;
import com.github.k1mb1.vkr_backend.domain.students.requests.UpdateStudentRequest;
import com.github.k1mb1.vkr_backend.domain.students.responses.StudentResponse;
import com.github.k1mb1.vkr_backend.domain.subjects.SubjectMapper;
import com.github.k1mb1.vkr_backend.domain.subjects.responses.SubjectResponse;
import jakarta.persistence.EntityNotFoundException;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentService {

    final StudentRepository studentRepository;
    final StudentMapper studentMapper;
    final StudentGroupRepository studentGroupRepository;
    final SubjectMapper subjectMapper;

    public Page<StudentResponse> findAllByFilter(StudentFilter filter, Pageable pageable) {
        return studentRepository
            .findAll(filter.toSpecification(), pageable)
            .map(studentMapper::toResponse);
    }

    @Transactional
    public StudentResponse create(CreateStudentRequest request) {
        var entity = studentMapper.toEntity(request);
        if (request.groupId() != null) {
            var group = studentGroupRepository
                .findById(request.groupId())
                .orElseThrow(() -> new EntityNotFoundException(
                    NOT_FOUND_MESSAGE.formatted("Group", request.groupId())
                ));
            entity.setGroup(group);
        }
        return studentMapper.toResponse(studentRepository.save(entity));
    }

    @Transactional
    public StudentResponse update(UUID studentId, UpdateStudentRequest request) {
        var student = getStudentById(studentId);
        studentMapper.update(student, request);
        if (request.groupId() != null) {
            var group = studentGroupRepository
                .findById(request.groupId())
                .orElseThrow(() -> new EntityNotFoundException(
                    NOT_FOUND_MESSAGE.formatted("Group", request.groupId())
                ));
            student.setGroup(group);
        }
        return studentMapper.toResponse(studentRepository.save(student));
    }

    @Transactional
    public void delete(UUID studentId) {
        var student = getStudentById(studentId);
        studentRepository.delete(student);
    }

    public List<SubjectResponse> findSubjectsByStudentId(UUID studentId) {
        var student = getStudentById(studentId);

        return student.getSubjects().stream()
            .sorted(Comparator.comparing(subject -> subject.getName().toLowerCase()))
            .map(subjectMapper::toResponse)
            .toList();
    }

    private StudentEntity getStudentById(UUID studentId) {
        return studentRepository
            .findById(studentId)
            .orElseThrow(() -> new EntityNotFoundException(
                NOT_FOUND_MESSAGE.formatted("Student", studentId)
            ));
    }
}
