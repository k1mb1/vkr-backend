package com.github.k1mb1.vkr_backend.teachers.internal;

import com.github.k1mb1.vkr_backend.teachers.TeachersApi;
import com.github.k1mb1.vkr_backend.teachers.domain.Teacher;
import com.github.k1mb1.vkr_backend.teachers.web.requests.CreateOrUpdateTeacherRequest;
import com.github.k1mb1.vkr_backend.teachers.web.responses.TeacherResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
class TeachersService implements TeachersApi {

    final TeachersRepository teacherRepository;

    @Override
    public TeacherResponse createOrUpdate(UUID id, CreateOrUpdateTeacherRequest request) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseGet(() -> {
                    Teacher createdTeacher = new Teacher();
                    createdTeacher.setId(id);
                    return createdTeacher;
                });

        teacher.setUsername(request.username());
        teacher.setEmail(request.email());

        Teacher savedTeacher = teacherRepository.save(teacher);
        return new TeacherResponse(savedTeacher.getId(), savedTeacher.getUsername(), savedTeacher.getEmail());
    }

}
