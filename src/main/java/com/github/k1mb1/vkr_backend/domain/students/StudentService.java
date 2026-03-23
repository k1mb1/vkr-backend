package com.github.k1mb1.vkr_backend.domain.students;

import com.github.k1mb1.vkr_backend.domain.student_groups.StudentGroupService;
import com.github.k1mb1.vkr_backend.domain.students.requests.CreateStudentRequest;
import com.github.k1mb1.vkr_backend.domain.students.requests.UpdateStudentRequest;
import com.github.k1mb1.vkr_backend.domain.students.responses.StudentResponse;
import jakarta.persistence.EntityNotFoundException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentService {

    final StudentRepository studentRepository;
    final StudentGroupService groupService;
    final StudentMapper studentMapper;

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
        var entity = studentRepository
            .findById(id)
            .orElseThrow(() ->
                new EntityNotFoundException("Student not found: " + id)
            );
        if (request.groupId() != null) {
            entity.setGroup(groupService.findEntityById(request.groupId()));
        }
        studentMapper.update(entity, request);
        return studentMapper.toResponse(studentRepository.save(entity));
    }
}
