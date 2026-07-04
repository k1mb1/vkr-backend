package com.github.k1mb1.vkr_backend.teacher.internal;

import com.github.k1mb1.vkr_backend.teacher.TeacherReferenceService;
import com.github.k1mb1.vkr_backend.teacher.domain.Teacher;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
class TeacherReferenceServiceImpl implements TeacherReferenceService {

    final TeacherRepository teacherRepository;

    @Override
    public Teacher getTeacherReferenceById(UUID id) {
        return teacherRepository.getReferenceById(id);
    }
}
