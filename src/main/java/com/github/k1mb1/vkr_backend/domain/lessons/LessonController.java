package com.github.k1mb1.vkr_backend.domain.lessons;

import com.github.k1mb1.vkr_backend.apis.LessonApi;
import com.github.k1mb1.vkr_backend.domain.lessons.filters.FindLessonsFilter;
import com.github.k1mb1.vkr_backend.domain.lessons.requests.BulkScheduleRequest;
import com.github.k1mb1.vkr_backend.domain.lessons.requests.CreateLessonRequest;
import com.github.k1mb1.vkr_backend.domain.lessons.requests.CreateLessonsByTypeRequest;
import com.github.k1mb1.vkr_backend.domain.lessons.requests.UpdateIssuedTaskIndexRequest;
import com.github.k1mb1.vkr_backend.domain.lessons.requests.UpdateLessonRequest;
import com.github.k1mb1.vkr_backend.domain.lessons.responses.LessonResponse;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class LessonController implements LessonApi {

    final LessonService lessonService;

    @Override
    public ResponseEntity<List<LessonResponse>> findAll(FindLessonsFilter filter) {
        var lessonFilter = LessonFilter.builder()
            .subjectId(filter.subjectId())
            .build();
        return ResponseEntity.ok(lessonService.findAll(lessonFilter));
    }

    @Override
    public ResponseEntity<LessonResponse> create(CreateLessonRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(lessonService.create(request));
    }

    @Override
    public ResponseEntity<List<LessonResponse>> createByType(CreateLessonsByTypeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(lessonService.createByType(request));
    }

    @Override
    public ResponseEntity<List<LessonResponse>> bulkSchedule(BulkScheduleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(lessonService.bulkSchedule(request));
    }

    @Override
    public ResponseEntity<LessonResponse> update(UUID id, UpdateLessonRequest request) {
        return ResponseEntity.ok(lessonService.update(id, request));
    }

    @Override
    public ResponseEntity<LessonResponse> archive(UUID id) {
        return ResponseEntity.ok(lessonService.archive(id));
    }

    @Override
    public ResponseEntity<Void> delete(UUID id) {
        lessonService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<LessonResponse> issueLesson(UUID id) {
        return ResponseEntity.ok(lessonService.issueLesson(id));
    }

    @Override
    public ResponseEntity<LessonResponse> updateIssuedTaskIndex(
        UUID id,
        UpdateIssuedTaskIndexRequest request
    ) {
        return ResponseEntity.ok(lessonService.updateIssuedTaskIndex(id, request));
    }
}
