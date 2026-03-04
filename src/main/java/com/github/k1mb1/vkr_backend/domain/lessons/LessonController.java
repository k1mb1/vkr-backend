package com.github.k1mb1.vkr_backend.domain.lessons;

import com.github.k1mb1.vkr_backend.apis.LessonApi;
import com.github.k1mb1.vkr_backend.domain.lessons.requests.CreateLessonRequest;
import com.github.k1mb1.vkr_backend.domain.lessons.requests.UpdateLessonRequest;
import com.github.k1mb1.vkr_backend.domain.lessons.responses.LessonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class LessonController implements LessonApi {

    private final LessonService lessonService;

    @Override
    public ResponseEntity<Page<LessonResponse>> findAll(LessonFilter filter, Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(lessonService.findAll(filter, pageable));
    }

    @Override
    public ResponseEntity<List<LessonResponse>> findAllBySubjectId(UUID subjectId) {
        return ResponseEntity.status(HttpStatus.OK).body(lessonService.findAllBySubjectId(subjectId));
    }

    @Override
    public ResponseEntity<List<LessonResponse>> findAllByStudentId(UUID studentId) {
        return ResponseEntity.status(HttpStatus.OK).body(lessonService.findAllByStudentId(studentId));
    }

    @Override
    public ResponseEntity<LessonResponse> findById(UUID id) {
        return ResponseEntity.status(HttpStatus.OK).body(lessonService.findById(id));
    }

    @Override
    public ResponseEntity<LessonResponse> create(CreateLessonRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(lessonService.create(request));
    }

    @Override
    public ResponseEntity<LessonResponse> update(UUID id, UpdateLessonRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(lessonService.update(id, request));
    }

    @Override
    public ResponseEntity<Void> delete(UUID id) {
        lessonService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}