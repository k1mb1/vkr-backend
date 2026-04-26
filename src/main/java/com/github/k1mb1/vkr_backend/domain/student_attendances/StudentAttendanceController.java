package com.github.k1mb1.vkr_backend.domain.student_attendances;

import com.github.k1mb1.vkr_backend.apis.StudentAttendanceApi;
import com.github.k1mb1.vkr_backend.domain.student_attendances.requests.UpsertAttendanceRequest;
import com.github.k1mb1.vkr_backend.domain.student_attendances.responses.AttendanceEntryResponse;
import com.github.k1mb1.vkr_backend.domain.student_attendances.responses.StudentAttendanceTableResponse;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class StudentAttendanceController implements StudentAttendanceApi {

    final StudentAttendanceService attendanceService;

    @Override
    public ResponseEntity<List<StudentAttendanceTableResponse>> findBySubject(UUID subjectId) {
        return ResponseEntity.ok(attendanceService.findBySubjectId(subjectId));
    }

    @Override
    public ResponseEntity<AttendanceEntryResponse> upsert(
        UUID lessonId,
        UpsertAttendanceRequest request
    ) {
        return ResponseEntity.ok(attendanceService.upsert(lessonId, request));
    }
}
