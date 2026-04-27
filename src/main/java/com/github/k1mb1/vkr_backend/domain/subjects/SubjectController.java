package com.github.k1mb1.vkr_backend.domain.subjects;

import com.github.k1mb1.vkr_backend.apis.SubjectApi;
import com.github.k1mb1.vkr_backend.domain.student_grades.StudentTaskGradeService;
import com.github.k1mb1.vkr_backend.domain.student_grades.responses.SubjectGradesTableResponse;
import com.github.k1mb1.vkr_backend.domain.subjects.filters.FindSubjectsFilter;
import com.github.k1mb1.vkr_backend.domain.subjects.requests.CreateSubjectRequest;
import com.github.k1mb1.vkr_backend.domain.subjects.requests.UpdateSubjectRequest;
import com.github.k1mb1.vkr_backend.domain.subjects.responses.AttachGroupToSubjectResponse;
import com.github.k1mb1.vkr_backend.domain.subjects.responses.FinalGradeResponse;
import com.github.k1mb1.vkr_backend.domain.subjects.responses.SubjectResponse;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SubjectController implements SubjectApi {

    final SubjectService subjectService;
    final StudentTaskGradeService gradeService;

    @Override
    public ResponseEntity<List<SubjectResponse>> findAllByTeacherId(
        UUID teacherId,
        FindSubjectsFilter filter
    ) {
        var subjectFilter = SubjectFilter.builder()
            .teacherId(teacherId)
            .archived(filter.archived() == null ? false : filter.archived())
            .build();
        return ResponseEntity.ok(subjectService.findAllByFilter(subjectFilter));
    }

    @Override
    public ResponseEntity<SubjectResponse> create(CreateSubjectRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(subjectService.create(request));
    }

    @Override
    public ResponseEntity<SubjectResponse> update(UUID subjectId, UpdateSubjectRequest request) {
        return ResponseEntity.ok(subjectService.update(subjectId, request));
    }

    @Override
    public ResponseEntity<SubjectResponse> archive(UUID subjectId) {
        return ResponseEntity.ok(
            subjectService.update(
                subjectId,
                UpdateSubjectRequest.builder().archived(true).archivedAt(Instant.now()).build()
            )
        );
    }

    @Override
    public ResponseEntity<AttachGroupToSubjectResponse> attachGroup(
        UUID subjectId,
        UUID groupId
    ) {
        return ResponseEntity.ok(subjectService.attachGroup(subjectId, groupId));
    }

    @Override
    public ResponseEntity<SubjectGradesTableResponse> findGrades(UUID subjectId) {
        return ResponseEntity.ok(gradeService.findGradesBySubjectId(subjectId));
    }

    @Override
    public ResponseEntity<List<FinalGradeResponse>> findFinalGrades(UUID subjectId) {
        return ResponseEntity.ok(gradeService.computeFinalGrades(subjectId));
    }
}
