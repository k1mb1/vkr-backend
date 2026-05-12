package com.github.k1mb1.vkr_backend.teacher.internal;

import com.github.k1mb1.vkr_backend.teacher.TeachersApi;
import com.github.k1mb1.vkr_backend.teacher.web.response.TeacherResponse;
import com.github.k1mb1.vkr_backend.teacher.domain.Teacher;
import com.github.k1mb1.vkr_backend.teacher.web.requests.CreateOrUpdateTeacherRequest;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
class TeacherService implements TeachersApi {

    final TeacherRepository teacherRepository;

    final TeacherMapper teacherMapper;

    @Transactional
    @Override
    public TeacherResponse createOrUpdateTeacher(UUID id, CreateOrUpdateTeacherRequest request) {
        var teacher = teacherRepository
            .findById(id)
            .orElseGet(() -> Teacher.builder().id(id).build());

        teacherMapper.updateEntity(request, teacher);
        return teacherMapper.toResponse(teacherRepository.save(teacher));
    }
}
