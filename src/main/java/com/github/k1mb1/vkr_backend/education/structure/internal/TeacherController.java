package com.github.k1mb1.vkr_backend.education.structure.internal;

import com.github.k1mb1.vkr_backend.education.structure.api.TeacherApi;
import com.github.k1mb1.vkr_backend.education.structure.api.requests.CreateOrUpdateTeacherRequest;
import com.github.k1mb1.vkr_backend.education.structure.api.responses.TeacherResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Validated
class TeacherController implements TeacherApi {
    final TeacherService teacherService;

    @Override
    public ResponseEntity<TeacherResponse> createOrUpdate(UUID id, CreateOrUpdateTeacherRequest request) {
        return ResponseEntity.ok(teacherService.createOrUpdate(id, request));
    }
}
