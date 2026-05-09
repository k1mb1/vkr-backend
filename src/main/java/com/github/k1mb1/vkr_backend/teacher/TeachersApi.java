package com.github.k1mb1.vkr_backend.teacher;

import com.github.k1mb1.vkr_backend.teacher.web.requests.CreateOrUpdateTeacherRequest;
import java.util.UUID;

public interface TeachersApi {

    TeacherResponse createOrUpdate(UUID id, CreateOrUpdateTeacherRequest request);
}
