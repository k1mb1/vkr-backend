package com.github.k1mb1.vkr_backend.education.lessons.internal;

import com.github.k1mb1.vkr_backend.education.lessons.api.LessonApi;
import com.github.k1mb1.vkr_backend.education.lessons.api.LessonFilterRequest;
import com.github.k1mb1.vkr_backend.education.lessons.api.requests.*;
import com.github.k1mb1.vkr_backend.education.lessons.api.responses.LessonResponse;
import java.util.List;
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
class LessonController implements LessonApi {
    final LessonService lessonService;

    @Override
    public ResponseEntity<Page<LessonResponse>> findAll(LessonFilterRequest filter, Pageable pageable) {
        return new ResponseEntity<>(lessonService.findAll(filter, pageable), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<LessonResponse> create(CreateLessonRequest request) {
        return new ResponseEntity<>(lessonService.create(request), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<List<LessonResponse>> bulkSchedule(BulkScheduleRequest request) {
        return new ResponseEntity<>(lessonService.bulkSchedule(request), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<LessonResponse> update(UUID id, UpdateLessonRequest request) {
        return new ResponseEntity<>(lessonService.update(id, request), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<LessonResponse> issue(UUID id) {
        return new ResponseEntity<>(lessonService.issueLesson(id), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<LessonResponse> updateIssuedTaskIndex(UUID id, UpdateIssuedTaskIndexRequest request) {
        return new ResponseEntity<>(lessonService.updateIssuedTaskIndex(id, request), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> delete(UUID id) {
        lessonService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
