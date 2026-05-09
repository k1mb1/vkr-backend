package com.github.k1mb1.vkr_backend.teacher.internal;

import com.github.k1mb1.vkr_backend.teacher.domain.Teacher;
import com.github.k1mb1.vkr_backend.teacher.web.requests.CreateOrUpdateTeacherRequest;
import com.github.k1mb1.vkr_backend.teacher.web.responses.TeacherResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final TeacherMapper teacherMapper;

    @Transactional
    public TeacherResponse createOrUpdate(UUID id, CreateOrUpdateTeacherRequest request) {
        var teacher = teacherRepository
            .findById(id)
            .orElseGet(() -> Teacher.builder().id(id).build());

        teacherMapper.updateEntity(request, teacher);
        return teacherMapper.toResponse(teacherRepository.save(teacher););
    }

    public TeacherResponse getById(UUID id) {
        var teacher = teacherRepository
            .findById(id)
            .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(
                "Teacher not found: " + id
            ));
        return teacherMapper.toResponse(teacher);
    }
}
