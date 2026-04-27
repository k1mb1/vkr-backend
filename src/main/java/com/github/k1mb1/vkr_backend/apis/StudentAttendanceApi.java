package com.github.k1mb1.vkr_backend.apis;

import com.github.k1mb1.vkr_backend.domain.student_attendances.requests.UpsertAttendanceRequest;
import com.github.k1mb1.vkr_backend.domain.student_attendances.responses.AttendanceEntryResponse;
import com.github.k1mb1.vkr_backend.domain.student_attendances.responses.SubjectAttendanceTableResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Student Attendance", description = "Attendance management")
public interface StudentAttendanceApi {

    @Operation(summary = "Full attendance table for a subject grouped by student")
    @GetMapping("/subjects/{subjectId}/attendance")
    ResponseEntity<SubjectAttendanceTableResponse> findBySubject(
        @PathVariable UUID subjectId
    );

    @Operation(
        summary = "Upsert student attendance for a lesson",
        description = "Creates or updates the attendance record for the given (lesson, student) pair."
    )
    @PutMapping("/lessons/{lessonId}/attendance")
    ResponseEntity<AttendanceEntryResponse> upsert(
        @PathVariable UUID lessonId,
        @RequestBody @Valid UpsertAttendanceRequest request
    );
}
