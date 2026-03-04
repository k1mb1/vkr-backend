package com.github.k1mb1.vkr_backend.domain.students;

import com.github.k1mb1.vkr_backend.apis.StudentApi;
import com.github.k1mb1.vkr_backend.domain.students.requests.CreateStudentRequest;
import com.github.k1mb1.vkr_backend.domain.students.requests.UpdateStudentRequest;
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
public class StudentController implements StudentApi {

    private final StudentService studentService;

    @Override
    public ResponseEntity<Page<StudentResponse>> findAll(StudentFilter filter, Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(studentService.findAll(filter, pageable));
    }

    @Override
    public ResponseEntity<List<StudentResponse>> findAllBySubjectId(UUID subjectId) {
        return ResponseEntity.status(HttpStatus.OK).body(studentService.findAllBySubjectId(subjectId));
    }

    @Override
    public ResponseEntity<List<StudentResponse>> findAllByGroupId(UUID groupId) {
        return ResponseEntity.status(HttpStatus.OK).body(studentService.findAllByGroupId(groupId));
    }

    @Override
    public ResponseEntity<StudentResponse> findById(UUID id) {
        return ResponseEntity.status(HttpStatus.OK).body(studentService.findById(id));
    }

    @Override
    public ResponseEntity<StudentResponse> create(CreateStudentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(studentService.create(request));
    }

    @Override
    public ResponseEntity<StudentResponse> update(UUID id, UpdateStudentRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(studentService.update(id, request));
    }

    @Override
    public ResponseEntity<Void> delete(UUID id) {
        studentService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}