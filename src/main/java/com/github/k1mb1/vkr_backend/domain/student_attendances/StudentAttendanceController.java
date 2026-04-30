package com.github.k1mb1.vkr_backend.domain.student_attendances;

import com.github.k1mb1.vkr_backend.apis.StudentAttendanceApi;
import com.github.k1mb1.vkr_backend.domain.student_attendances.filters.FindAttendanceFilter;
import com.github.k1mb1.vkr_backend.domain.student_attendances.requests.UpsertAttendanceRequest;
import com.github.k1mb1.vkr_backend.domain.student_attendances.responses.AttendanceEntryResponse;
import com.github.k1mb1.vkr_backend.domain.student_attendances.responses.AttendanceTableResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Validated
public class StudentAttendanceController implements StudentAttendanceApi {

    final StudentAttendanceService attendanceService;

    @Override
    public ResponseEntity<AttendanceTableResponse> findBySubject(
        UUID subjectId,
        FindAttendanceFilter filter
    ) {
        return ResponseEntity.ok(attendanceService.findBySubjectId(subjectId, filter));
    }

    @Override
    public ResponseEntity<AttendanceEntryResponse> upsert(
        UUID lessonId,
        UpsertAttendanceRequest request
    ) {
        return ResponseEntity.ok(attendanceService.upsert(lessonId, request));
    }
}
