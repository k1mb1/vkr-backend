package com.github.k1mb1.vkr_backend.domain.teachers;

import com.github.k1mb1.vkr_backend.apis.TeacherApi;
import com.github.k1mb1.vkr_backend.domain.teachers.requests.CreateTeacherRequest;
import com.github.k1mb1.vkr_backend.domain.teachers.requests.FindOrCreateTeacherRequest;
import com.github.k1mb1.vkr_backend.domain.teachers.requests.UpdateTeacherRequest;
import com.github.k1mb1.vkr_backend.domain.teachers.responses.TeacherDetailsResponse;
import com.github.k1mb1.vkr_backend.domain.teachers.responses.TeacherResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
public class TeacherController implements TeacherApi {

    final TeacherService teacherService;

    @Override
    public ResponseEntity<Page<TeacherResponse>> findAll(TeacherFilter filter, Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(teacherService.findAll(filter, pageable));
    }

    @Override
    public ResponseEntity<TeacherResponse> findOrCreate(@AuthenticationPrincipal Jwt jwt, FindOrCreateTeacherRequest request) {
        log.info("JWT claims: {}", jwt.getClaims());
        var id = UUID.fromString(jwt.getSubject());
        return ResponseEntity.status(HttpStatus.OK).body(teacherService.findOrCreate(id, request.username(), request.email()));
    }

    @Override
    public ResponseEntity<List<TeacherResponse>> findAllBySubjectId(UUID subjectId) {
        return ResponseEntity.status(HttpStatus.OK).body(teacherService.findAllBySubjectId(subjectId));
    }

    @Override
    @PreAuthorize("@securityService.isSameUser(authentication, #id)")
    public ResponseEntity<TeacherDetailsResponse> findById(UUID id) {
        return ResponseEntity.status(HttpStatus.OK).body(teacherService.findById(id));
    }

    @Override
    public ResponseEntity<TeacherResponse> create(CreateTeacherRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(teacherService.create(request));
    }

    @Override
    public ResponseEntity<TeacherResponse> update(UUID id, UpdateTeacherRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(teacherService.update(id, request));
    }

    @Override
    public ResponseEntity<Void> delete(UUID id) {
        teacherService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}