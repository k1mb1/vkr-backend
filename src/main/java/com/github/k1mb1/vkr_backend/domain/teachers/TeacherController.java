package com.github.k1mb1.vkr_backend.domain.teachers;

import com.github.k1mb1.vkr_backend.apis.TeacherApi;
import com.github.k1mb1.vkr_backend.domain.teachers.requests.UpdateTeacherRequest;
import com.github.k1mb1.vkr_backend.domain.teachers.responses.TeacherResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class TeacherController implements TeacherApi {

    final TeacherService teacherService;

    @Override
    public ResponseEntity<TeacherResponse> createOrUpdate(
        UUID id,
        UpdateTeacherRequest request
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(
            teacherService.createOrUpdate(id, request)
        );
    }
}
