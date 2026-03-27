package com.github.k1mb1.vkr_backend.domain.subjects;

import com.github.k1mb1.vkr_backend.apis.SubjectStudentApi;
import com.github.k1mb1.vkr_backend.domain.student_grades.StudentGradeService;
import com.github.k1mb1.vkr_backend.domain.student_grades.responses.StudentGradesResponse;
import com.github.k1mb1.vkr_backend.domain.students.responses.StudentResponse;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SubjectStudentController implements SubjectStudentApi {

    final SubjectService subjectService;
    final StudentGradeService studentGradeService;

    @Override
    public ResponseEntity<Page<StudentResponse>> findStudentsBySubject(
        UUID subjectId,
        Pageable pageable
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(
            subjectService.findStudentsBySubject(subjectId, pageable)
        );
    }

    @Override
    public ResponseEntity<List<StudentGradesResponse>> findGradesBySubject(
        UUID subjectId
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(
            studentGradeService.findGradesBySubject(subjectId)
        );
    }

    @Override
    public ResponseEntity<Void> addStudentToSubject(
        UUID subjectId,
        UUID studentId
    ) {
        subjectService.addStudentToSubject(subjectId, studentId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Override
    public ResponseEntity<Void> addStudentsByUsernames(
        UUID subjectId,
        List<String> usernames
    ) {
        subjectService.addStudentsToSubjectByUsernames(subjectId, usernames);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Override
    public ResponseEntity<Void> removeStudentFromSubject(
        UUID subjectId,
        UUID studentId
    ) {
        subjectService.removeStudentFromSubject(subjectId, studentId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
