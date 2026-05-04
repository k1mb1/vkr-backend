package com.github.k1mb1.vkr_backend.education.assignments.internal;

import com.github.k1mb1.vkr_backend.education.assignments.api.GradeApi;
import com.github.k1mb1.vkr_backend.education.assignments.api.GradeFilter;
import com.github.k1mb1.vkr_backend.education.assignments.api.requests.UpsertTaskGradeRequest;
import com.github.k1mb1.vkr_backend.education.assignments.api.responses.FinalGradeResponse;
import com.github.k1mb1.vkr_backend.education.assignments.api.responses.FindGradesFilter;
import com.github.k1mb1.vkr_backend.education.assignments.api.responses.GradeMatrixResponse;
import com.github.k1mb1.vkr_backend.education.assignments.api.responses.GradeResponse;
import com.github.k1mb1.vkr_backend.education.assignments.api.responses.GradeTableResponse;
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
class GradeController implements GradeApi {
    final StudentTaskGradeService gradeService;

    @Override
    public ResponseEntity<GradeMatrixResponse> findGradesBySubjectId(UUID subjectId, FindGradesFilter filter) {
        return new ResponseEntity<>(gradeService.findGradesBySubjectId(subjectId, filter), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<GradeTableResponse> findGradesByLesson(UUID lessonId) {
        return new ResponseEntity<>(gradeService.findGradesByLesson(lessonId), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<FinalGradeResponse>> computeFinalGrades(UUID subjectId) {
        return new ResponseEntity<>(gradeService.computeFinalGrades(subjectId), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Page<GradeResponse>> findGrades(UUID lessonId, UUID taskId, GradeFilter filter, Pageable pageable) {
        return new ResponseEntity<>(gradeService.findGrades(taskId, filter, pageable), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<GradeResponse> upsert(UUID lessonId, UUID taskId, UpsertTaskGradeRequest request) {
        return new ResponseEntity<>(gradeService.upsert(lessonId, taskId, request), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<GradeResponse>> upsertBulk(UUID lessonId, UUID taskId, List<UpsertTaskGradeRequest> requests) {
        return new ResponseEntity<>(gradeService.upsertBulk(lessonId, taskId, requests), HttpStatus.OK);
    }
}
