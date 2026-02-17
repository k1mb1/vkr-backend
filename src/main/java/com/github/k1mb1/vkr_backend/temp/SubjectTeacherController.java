package com.github.k1mb1.vkr_backend.temp;

import com.github.k1mb1.vkr_backend.apis.SubjectTeacherApi;
import com.github.k1mb1.vkr_backend.domain.teachers.responses.TeacherResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class SubjectTeacherController implements SubjectTeacherApi {

    private final SubjectTeacherService subjectTeacherService;

    @Override
    public ResponseEntity<Page<TeacherResponse>> findTeachersBySubject(UUID subjectId, Pageable pageable) {
//        return ResponseEntity.status(HttpStatus.OK).body(subjectTeacherService.findTeachersBySubject(subjectId, pageable));
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @Override
    public ResponseEntity<Void> addTeacherToSubject(UUID subjectId, UUID teacherId) {
//        subjectTeacherService.addTeacherToSubject(subjectId, teacherId);
//        return ResponseEntity.status(HttpStatus.CREATED).build();
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @Override
    public ResponseEntity<Void> removeTeacherFromSubject(UUID subjectId, UUID teacherId) {
//        subjectTeacherService.removeTeacherFromSubject(subjectId, teacherId);
//        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}