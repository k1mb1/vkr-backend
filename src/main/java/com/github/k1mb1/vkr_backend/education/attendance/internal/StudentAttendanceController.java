package com.github.k1mb1.vkr_backend.education.attendance.internal;

import com.github.k1mb1.vkr_backend.education.attendance.api.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Attendance", description = "Attendance management")
class StudentAttendanceController {
    final StudentAttendanceService attendanceService;

    @GetMapping("/subjects/{subjectId}/attendance")
    ResponseEntity<AttendanceTableResponse> findBySubjectId(
        @PathVariable UUID subjectId,
        @ParameterObject @ModelAttribute FindAttendanceFilter filter
    ) {
        return new ResponseEntity<>(attendanceService.findBySubjectId(subjectId, filter), HttpStatus.OK);
    }

    @PutMapping("/lessons/{lessonId}/attendance")
    ResponseEntity<AttendanceEntryResponse> upsert(
        @PathVariable UUID lessonId,
        @RequestBody @Valid UpsertAttendanceRequest request
    ) {
        return new ResponseEntity<>(attendanceService.upsert(lessonId, request), HttpStatus.OK);
    }
}
