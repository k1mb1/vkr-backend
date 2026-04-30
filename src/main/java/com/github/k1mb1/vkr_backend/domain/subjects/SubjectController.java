package com.github.k1mb1.vkr_backend.domain.subjects;

import com.github.k1mb1.vkr_backend.apis.SubjectApi;
import com.github.k1mb1.vkr_backend.domain.student_grades.StudentTaskGradeService;
import com.github.k1mb1.vkr_backend.domain.student_grades.filters.FindGradesFilter;
import com.github.k1mb1.vkr_backend.domain.student_grades.responses.SubjectGradeTableResponse;
import com.github.k1mb1.vkr_backend.domain.subjects.filters.FindSubjectsFilter;
import com.github.k1mb1.vkr_backend.domain.subjects.requests.CreateSubjectRequest;
import com.github.k1mb1.vkr_backend.domain.subjects.requests.UpdateSubjectRequest;
import com.github.k1mb1.vkr_backend.domain.subjects.responses.GroupAttachmentResponse;
import com.github.k1mb1.vkr_backend.domain.subjects.responses.FinalGradeResponse;
import com.github.k1mb1.vkr_backend.domain.subjects.responses.SubjectResponse;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Validated
public class SubjectController implements SubjectApi {

    final SubjectService subjectService;
    final StudentTaskGradeService gradeService;

    @Override
    public ResponseEntity<List<SubjectResponse>> findAllByTeacherId(
        UUID teacherId,
        FindSubjectsFilter filter
    ) {
        var spec = filter.toServiceFilter(teacherId);
        return ResponseEntity.ok(subjectService.findAll(spec));
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
    public ResponseEntity<GroupAttachmentResponse> attachGroup(
        UUID subjectId,
        UUID groupId
    ) {
        return ResponseEntity.ok(subjectService.attachGroup(subjectId, groupId));
    }

    @Override
    public ResponseEntity<SubjectGradeTableResponse> findGrades(
        UUID subjectId,
        FindGradesFilter filter
    ) {
        return ResponseEntity.ok(gradeService.findGradesBySubjectId(subjectId, filter));
    }

    @Override
    public ResponseEntity<List<FinalGradeResponse>> findFinalGrades(UUID subjectId) {
        return ResponseEntity.ok(gradeService.computeFinalGrades(subjectId));
    }

    @Override
    public ResponseEntity<Void> remove(UUID subjectId) {
        subjectService.remove(subjectId);
        return ResponseEntity.noContent().build();
    }
}
