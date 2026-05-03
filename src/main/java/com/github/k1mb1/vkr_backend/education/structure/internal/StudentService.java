package com.github.k1mb1.vkr_backend.education.structure.internal;

import static com.github.k1mb1.vkr_backend.shared.web.ErrorMessages.NOT_FOUND_MESSAGE;

import com.github.k1mb1.vkr_backend.education.structure.api.StudentFilter;
import com.github.k1mb1.vkr_backend.education.structure.api.StudentMovedEvent;
import com.github.k1mb1.vkr_backend.education.structure.api.requests.CreateStudentRequest;
import com.github.k1mb1.vkr_backend.education.structure.api.requests.UpdateStudentRequest;
import com.github.k1mb1.vkr_backend.education.structure.api.responses.StudentResponse;
import jakarta.persistence.EntityNotFoundException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
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
    final ApplicationEventPublisher eventPublisher;

    public Page<StudentResponse> findAllByFilter(StudentFilter filter, Pageable pageable) {
        return studentRepository.findAll(filter.toSpecification(), pageable).map(studentMapper::toResponse);
    }

    @Transactional
    public StudentResponse create(CreateStudentRequest request) {
        var entity = studentMapper.toEntity(request);
        if (request.groupId() != null) {
            var group = studentGroupRepository.findById(request.groupId())
                .orElseThrow(() -> new EntityNotFoundException(NOT_FOUND_MESSAGE.formatted("Group", request.groupId())));
            entity.setGroup(group);
        }
        return studentMapper.toResponse(studentRepository.save(entity));
    }

    @Transactional
    public StudentResponse update(UUID studentId, UpdateStudentRequest request) {
        var student = getStudentById(studentId);
        UUID oldGroupId = student.getGroup() != null ? student.getGroup().getId() : null;
        studentMapper.update(student, request);
        if (request.groupId() != null) {
            var group = studentGroupRepository.findById(request.groupId())
                .orElseThrow(() -> new EntityNotFoundException(NOT_FOUND_MESSAGE.formatted("Group", request.groupId())));
            student.setGroup(group);
            if (!request.groupId().equals(oldGroupId)) {
                eventPublisher.publishEvent(new StudentMovedEvent(studentId, oldGroupId, request.groupId()));
            }
        }
        return studentMapper.toResponse(studentRepository.save(student));
    }

    @Transactional
    public void delete(UUID studentId) {
        studentRepository.delete(getStudentById(studentId));
    }

    private StudentEntity getStudentById(UUID studentId) {
        return studentRepository.findById(studentId)
            .orElseThrow(() -> new EntityNotFoundException(NOT_FOUND_MESSAGE.formatted("Student", studentId)));
    }
}
