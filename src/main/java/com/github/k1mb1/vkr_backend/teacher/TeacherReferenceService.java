package com.github.k1mb1.vkr_backend.teacher;

import com.github.k1mb1.vkr_backend.teacher.domain.TeacherEntity;
import java.util.UUID;

public interface TeacherReferenceService {

    TeacherEntity getTeacherReferenceById(UUID id);
}
