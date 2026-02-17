package com.github.k1mb1.vkr_backend.domain.student_grades;

import com.github.k1mb1.vkr_backend.apis.GradeApi;
import com.github.k1mb1.vkr_backend.domain.student_grades.requests.UpdateStudentGradeRequest;
import com.github.k1mb1.vkr_backend.domain.student_grades.requests.CreateStudentGradeRequest;
import com.github.k1mb1.vkr_backend.domain.student_grades.responses.StudentGradeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class StudentGradeController implements GradeApi {

    private final StudentGradeService studentGradeService;

    @Override
    public ResponseEntity<Page<StudentGradeResponse>> findAll(StudentGradeFilter filter, Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(studentGradeService.findAll(filter, pageable));
    }

    @Override
    public ResponseEntity<StudentGradeResponse> findById(UUID id) {
        return ResponseEntity.status(HttpStatus.OK).body(studentGradeService.findById(id));
    }

    @Override
    public ResponseEntity<StudentGradeResponse> create(CreateStudentGradeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(studentGradeService.create(request));
    }

    @Override
    public ResponseEntity<StudentGradeResponse> update(UUID id, UpdateStudentGradeRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(studentGradeService.update(id, request));
    }

    @Override
    public ResponseEntity<Void> delete(UUID id) {
        studentGradeService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}