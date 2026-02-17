package com.github.k1mb1.vkr_backend.domain.student_attendances;

import com.github.k1mb1.vkr_backend.apis.AttendanceApi;
import com.github.k1mb1.vkr_backend.domain.student_attendances.requests.CreateStudentAttendanceRequest;
import com.github.k1mb1.vkr_backend.domain.student_attendances.requests.UpdateStudentAttendanceRequest;
import com.github.k1mb1.vkr_backend.domain.student_attendances.responses.StudentAttendanceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class StudentAttendanceController implements AttendanceApi {

    private final StudentAttendanceService studentAttendanceService;

    @Override
    public ResponseEntity<Page<StudentAttendanceResponse>> findAll(StudentAttendanceFilter filter, Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(studentAttendanceService.findAll(filter, pageable));
    }

    @Override
    public ResponseEntity<StudentAttendanceResponse> findById(UUID id) {
        return ResponseEntity.status(HttpStatus.OK).body(studentAttendanceService.findById(id));
    }

    @Override
    public ResponseEntity<StudentAttendanceResponse> create(CreateStudentAttendanceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(studentAttendanceService.create(request));
    }

    @Override
    public ResponseEntity<StudentAttendanceResponse> update(UUID id, UpdateStudentAttendanceRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(studentAttendanceService.update(id, request));
    }

    @Override
    public ResponseEntity<Void> delete(UUID id) {
        studentAttendanceService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}