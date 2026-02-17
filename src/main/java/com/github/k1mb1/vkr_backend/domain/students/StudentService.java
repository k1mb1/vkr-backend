package com.github.k1mb1.vkr_backend.domain.students;

import com.github.k1mb1.vkr_backend.domain.student_groups.StudentGroupService;
import com.github.k1mb1.vkr_backend.domain.students.requests.CreateStudentRequest;
import com.github.k1mb1.vkr_backend.domain.students.requests.UpdateStudentRequest;
import com.github.k1mb1.vkr_backend.domain.students.responses.StudentResponse;
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
public class StudentService {

    private final StudentRepository studentRepository;
    private final StudentGroupService groupService;
    private final StudentMapper studentMapper;

    public Page<StudentResponse> findAll(StudentFilter filter, Pageable pageable) {
        return studentRepository.findAll(filter.toSpecification(), pageable)
                .map(studentMapper::toResponse);
    }

    public StudentResponse findById(UUID id) {
        return studentRepository.findById(id)
                .map(studentMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Student not found: " + id));
    }

    public StudentEntity findEntityById(UUID id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Student not found: " + id));
    }

    @Transactional
    public StudentResponse create(CreateStudentRequest request) {
        var entity = studentMapper.toEntity(request);
        if (request.groupId() != null) {
            entity.setGroup(groupService.findEntityById(request.groupId()));
        }
        return studentMapper.toResponse(studentRepository.save(entity));
    }

    @Transactional
    public StudentResponse update(UUID id, UpdateStudentRequest request) {
        var entity = studentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Student not found: " + id));
        if (request.groupId() != null) {
            entity.setGroup(groupService.findEntityById(request.groupId()));
        }
        studentMapper.update(entity, request);
        return studentMapper.toResponse(studentRepository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        if (!studentRepository.existsById(id)) {
            throw new EntityNotFoundException("Student not found: " + id);
        }
        studentRepository.deleteById(id);
    }
}