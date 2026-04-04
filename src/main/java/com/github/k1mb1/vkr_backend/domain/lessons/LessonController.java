package com.github.k1mb1.vkr_backend.domain.lessons;

import com.github.k1mb1.vkr_backend.apis.LessonApi;
import com.github.k1mb1.vkr_backend.domain.lessons.requests.BulkScheduleLessonsRequest;
import com.github.k1mb1.vkr_backend.domain.lessons.requests.CreateLessonRequest;
import com.github.k1mb1.vkr_backend.domain.lessons.requests.UpdateDecayFactorRequest;
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
    public ResponseEntity<List<LessonResponse>> findAllBySubjectId(
        UUID subjectId
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(
            lessonService.findAllBySubjectId(subjectId)
        );
    }

    @Override
    public ResponseEntity<LessonResponse> create(CreateLessonRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
            lessonService.create(request)
        );
    }

    @Override
    public ResponseEntity<List<LessonResponse>> bulkSchedule(
        BulkScheduleLessonsRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
            lessonService.bulkSchedule(request)
        );
    }

    @Override
    public ResponseEntity<LessonResponse> updateDecayFactor(
        UUID id,
        UpdateDecayFactorRequest request
    ) {
        return ResponseEntity.ok(lessonService.updateDecayFactor(id, request));
    }
}
