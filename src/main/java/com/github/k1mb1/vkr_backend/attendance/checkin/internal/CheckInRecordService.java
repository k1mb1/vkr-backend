package com.github.k1mb1.vkr_backend.attendance.checkin.internal;

import com.github.k1mb1.vkr_backend.attendance.checkin.CheckInRecordsApi;
import com.github.k1mb1.vkr_backend.attendance.checkin.domain.CheckInRecord;
import com.github.k1mb1.vkr_backend.attendance.checkin.domain.CheckInSessionState;
import com.github.k1mb1.vkr_backend.attendance.checkin.web.requests.StudentCheckInRequest;
import com.github.k1mb1.vkr_backend.attendance.checkin.web.responses.CheckInRecordResponse;
import com.github.k1mb1.vkr_backend.common.error.ResourceNotFoundException;
import com.github.k1mb1.vkr_backend.lesson.LessonStudentsApi;
import com.github.k1mb1.vkr_backend.student.internal.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class CheckInRecordService
    implements CheckInRecordsApi {

    final CheckInSessionRepository sessionRepository;

    final CheckInRecordRepository recordRepository;

    final StudentRepository studentRepository;

    final LessonStudentsApi lessonStudentsApi;

    final CheckInRecordMapper mapper;

    @Transactional
    @Override
    public CheckInRecordResponse checkIn(UUID sessionId, StudentCheckInRequest request) {
        var session = sessionRepository.findWithDetailsById(sessionId)
            .orElseThrow(() -> new ResourceNotFoundException("CheckInSession", sessionId));

        var now = Instant.now();
        var state = session.stateAt(now);
        if (state != CheckInSessionState.OPEN && state != CheckInSessionState.LATE_WINDOW) {
            throw new IllegalStateException("Check-in is closed for session: " + sessionId);
        }

        var studentId = request.studentId();
        var students = lessonStudentsApi.studentsOf(session.getLessonScope());
        var inScope = students.stream().anyMatch(s -> s.getId().equals(studentId));
        if (!inScope) {
            throw new IllegalArgumentException("Student is not part of this lesson audience: " + studentId);
        }

        var status = session.statusForCheckInAt(now);
        if (status == null) {
            throw new IllegalStateException("Check-in window has elapsed");
        }

        var record = recordRepository.findBySessionIdAndStudentId(sessionId, studentId)
            .orElseGet(() -> CheckInRecord.builder()
                .session(session)
                .student(studentRepository.getReferenceById(studentId))
                .checkedInAt(now)
                .build());

        // first check-in wins; do not downgrade PRESENT to LATE on repeated submission
        if (record.getId() == null) {
            record.setStatus(status);
            record.setCheckedInAt(now);
        }

        return mapper.toResponse(recordRepository.save(record));
    }
}
