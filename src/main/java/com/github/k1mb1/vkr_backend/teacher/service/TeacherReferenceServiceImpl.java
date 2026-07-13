package com.github.k1mb1.vkr_backend.teacher.service;

import com.github.k1mb1.vkr_backend.teacher.TeacherReferenceService;
import com.github.k1mb1.vkr_backend.teacher.domain.TeacherEntity;
import com.github.k1mb1.vkr_backend.teacher.repository.TeacherRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TeacherReferenceServiceImpl implements TeacherReferenceService {

    final TeacherRepository teacherRepository;

    @Override
    public TeacherEntity getTeacherReferenceById(UUID id) {
        return teacherRepository.getReferenceById(id);
    }
}
