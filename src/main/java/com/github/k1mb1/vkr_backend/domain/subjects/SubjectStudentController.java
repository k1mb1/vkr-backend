package com.github.k1mb1.vkr_backend.domain.subjects;

import com.github.k1mb1.vkr_backend.apis.SubjectStudentApi;
import com.github.k1mb1.vkr_backend.domain.students.responses.StudentResponse;
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
public class SubjectStudentController implements SubjectStudentApi {

    private final SubjectService subjectService;

    @Override
    public ResponseEntity<Page<StudentResponse>> findStudentsBySubject(UUID subjectId, Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(subjectService.findStudentsBySubject(subjectId, pageable));
    }

    @Override
    public ResponseEntity<Void> addStudentToSubject(UUID subjectId, UUID studentId) {
        subjectService.addStudentToSubject(subjectId, studentId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Override
    public ResponseEntity<Void> addStudentsToSubject(UUID subjectId, List<UUID> studentIds) {
        subjectService.addStudentsToSubject(subjectId, studentIds);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Override
    public ResponseEntity<Void> removeStudentFromSubject(UUID subjectId, UUID studentId) {
        subjectService.removeStudentFromSubject(subjectId, studentId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
