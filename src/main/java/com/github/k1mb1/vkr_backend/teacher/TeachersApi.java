package com.github.k1mb1.vkr_backend.teacher;

import com.github.k1mb1.vkr_backend.teacher.web.requests.CreateOrUpdateTeacherRequest;
import com.github.k1mb1.vkr_backend.teacher.web.responses.TeacherResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;

public interface TeachersApi {

    ResponseEntity<TeacherResponse> createOrUpdate(
        UUID id,
        @Valid CreateOrUpdateTeacherRequest request
    );
}
