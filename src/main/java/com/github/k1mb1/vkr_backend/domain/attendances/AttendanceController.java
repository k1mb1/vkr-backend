package com.github.k1mb1.vkr_backend.domain.attendances;

import com.github.k1mb1.vkr_backend.apis.AttendanceApi;
import com.github.k1mb1.vkr_backend.domain.attendances.requests.CreateAttendanceRequest;
import com.github.k1mb1.vkr_backend.domain.attendances.requests.UpdateAttendanceRequest;
import com.github.k1mb1.vkr_backend.domain.attendances.responses.AttendanceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class AttendanceController implements AttendanceApi {

    private final AttendanceService attendanceService;

    @Override
    public ResponseEntity<Page<AttendanceResponse>> findAll(AttendanceFilter filter, Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(attendanceService.findAll(filter, pageable));
    }

    @Override
    public ResponseEntity<AttendanceResponse> findById(UUID id) {
        return ResponseEntity.status(HttpStatus.OK).body(attendanceService.findById(id));
    }

    @Override
    public ResponseEntity<AttendanceResponse> create(CreateAttendanceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(attendanceService.create(request));
    }

    @Override
    public ResponseEntity<AttendanceResponse> update(UUID id, UpdateAttendanceRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(attendanceService.update(id, request));
    }

    @Override
    public ResponseEntity<Void> delete(UUID id) {
        attendanceService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}