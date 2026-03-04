package com.github.k1mb1.vkr_backend.domain.subjects;

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

    private final SubjectService subjectService;

    @Override
    public ResponseEntity<Page<TeacherResponse>> findTeachersBySubject(UUID subjectId, Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(subjectService.findTeachersBySubject(subjectId, pageable));
    }

    @Override
    public ResponseEntity<Void> addTeacherToSubject(UUID subjectId, UUID teacherId) {
        subjectService.addTeacherToSubject(subjectId, teacherId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Override
    public ResponseEntity<Void> removeTeacherFromSubject(UUID subjectId, UUID teacherId) {
        subjectService.removeTeacherFromSubject(subjectId, teacherId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
