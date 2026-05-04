package com.github.k1mb1.vkr_backend.education.structure.internal;

import com.github.k1mb1.vkr_backend.education.structure.api.TeacherApi;
import com.github.k1mb1.vkr_backend.education.structure.api.TeacherFilter;
import com.github.k1mb1.vkr_backend.education.structure.api.requests.CreateOrUpdateTeacherRequest;
import com.github.k1mb1.vkr_backend.education.structure.api.responses.TeacherResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Validated
class TeacherController implements TeacherApi {
    final TeacherService teacherService;

    @Override
    public ResponseEntity<Page<TeacherResponse>> findAll(TeacherFilter filter, Pageable pageable) {
        return new ResponseEntity<>(teacherService.findAll(filter, pageable), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<TeacherResponse> createOrUpdate(UUID id, CreateOrUpdateTeacherRequest request) {
        return new ResponseEntity<>(teacherService.createOrUpdate(id, request), HttpStatus.OK);
    }
}
