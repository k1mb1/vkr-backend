package com.github.k1mb1.vkr_backend.teacher;

import com.github.k1mb1.vkr_backend.teacher.domain.Teacher;

import java.util.UUID;

public interface TeacherReferenceService {

    Teacher getTeacherReferenceById(UUID id);
}
