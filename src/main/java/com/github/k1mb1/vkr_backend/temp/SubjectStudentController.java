package com.github.k1mb1.vkr_backend.temp;

import com.github.k1mb1.vkr_backend.apis.SubjectStudentApi;
import com.github.k1mb1.vkr_backend.domain.students.responses.StudentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class SubjectStudentController implements SubjectStudentApi {

    private final SubjectStudentService subjectStudentService;

    @Override
    public ResponseEntity<Page<StudentResponse>> findStudentsBySubject(UUID subjectId, Pageable pageable) {
//        return ResponseEntity.status(HttpStatus.OK).body(subjectStudentService.findStudentsBySubject(subjectId, pageable));
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @Override
    public ResponseEntity<Void> addStudentToSubject(UUID subjectId, UUID studentId) {
//        subjectStudentService.addStudentToSubject(subjectId, studentId);
//        return ResponseEntity.status(HttpStatus.CREATED).build();
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @Override
    public ResponseEntity<Void> removeStudentFromSubject(UUID subjectId, UUID studentId) {
//        subjectStudentService.removeStudentFromSubject(subjectId, studentId);
//        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}