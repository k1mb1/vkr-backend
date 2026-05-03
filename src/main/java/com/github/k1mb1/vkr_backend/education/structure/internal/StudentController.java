package com.github.k1mb1.vkr_backend.education.structure.internal;

import com.github.k1mb1.vkr_backend.education.structure.api.StudentApi;
import com.github.k1mb1.vkr_backend.education.structure.api.StudentFilter;
import com.github.k1mb1.vkr_backend.education.structure.api.requests.CreateStudentRequest;
import com.github.k1mb1.vkr_backend.education.structure.api.requests.UpdateStudentRequest;
import com.github.k1mb1.vkr_backend.education.structure.api.responses.StudentResponse;
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
class StudentController implements StudentApi {
    final StudentService studentService;

    @Override
    public ResponseEntity<Page<StudentResponse>> findAll(StudentFilter filter, Pageable pageable) {
        return ResponseEntity.ok(studentService.findAllByFilter(filter, pageable));
    }

    @Override
    public ResponseEntity<StudentResponse> create(CreateStudentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(studentService.create(request));
    }

    @Override
    public ResponseEntity<StudentResponse> update(UUID studentId, UpdateStudentRequest request) {
        return ResponseEntity.ok(studentService.update(studentId, request));
    }

    @Override
    public ResponseEntity<Void> delete(UUID studentId) {
        studentService.delete(studentId);
        return ResponseEntity.noContent().build();
    }
}
