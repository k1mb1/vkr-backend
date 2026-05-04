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
        return new ResponseEntity<>(studentService.findAllByFilter(filter, pageable), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<StudentResponse> create(CreateStudentRequest request) {
        return new ResponseEntity<>(studentService.create(request), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<StudentResponse> update(UUID studentId, UpdateStudentRequest request) {
        return new ResponseEntity<>(studentService.update(studentId, request), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> delete(UUID studentId) {
        studentService.delete(studentId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
