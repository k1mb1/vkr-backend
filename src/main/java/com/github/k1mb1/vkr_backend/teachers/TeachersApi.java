package com.github.k1mb1.vkr_backend.teachers;

import com.github.k1mb1.vkr_backend.teachers.web.requests.CreateOrUpdateTeacherRequest;
import com.github.k1mb1.vkr_backend.teachers.web.responses.TeacherResponse;
import java.util.UUID;

public interface TeachersApi {
    TeacherResponse createOrUpdate(
        UUID id,
        CreateOrUpdateTeacherRequest request
    );
}
