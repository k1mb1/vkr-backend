package com.github.k1mb1.vkr_backend.attendance.checkin.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.github.k1mb1.vkr_backend.attendance.checkin.domain.CheckInRecord;
import com.github.k1mb1.vkr_backend.attendance.checkin.domain.CheckInRecordStatus;
import com.github.k1mb1.vkr_backend.attendance.checkin.domain.CheckInSession;
import com.github.k1mb1.vkr_backend.attendance.checkin.web.requests.StudentCheckInRequest;
import com.github.k1mb1.vkr_backend.attendance.checkin.web.responses.CheckInRecordResponse;
import com.github.k1mb1.vkr_backend.common.error.ResourceNotFoundException;
import com.github.k1mb1.vkr_backend.lesson.LessonStudentsApi;
import com.github.k1mb1.vkr_backend.lesson.domain.LessonScope;
import com.github.k1mb1.vkr_backend.student.domain.Student;
import com.github.k1mb1.vkr_backend.student.internal.StudentRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CheckInRecordServiceTest {

    @Mock CheckInSessionRepository sessionRepository;
    @Mock CheckInRecordRepository recordRepository;
    @Mock StudentRepository studentRepository;
    @Mock LessonStudentsApi lessonStudentsApi;
    @Mock CheckInRecordMapper mapper;

    @InjectMocks CheckInRecordService service;

    // -----------------------------------------------------------------------
    // checkIn — session not found
    // -----------------------------------------------------------------------

    @Test
    void checkIn_sessionNotFound_throwsResourceNotFoundException() {
        var sessionId = UUID.randomUUID();
        when(sessionRepository.findWithDetailsById(sessionId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.checkIn(sessionId, new StudentCheckInRequest(UUID.randomUUID())))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("CheckInSession");
    }

    // -----------------------------------------------------------------------
    // checkIn — session is closed (AWAITING_CONFIRMATION: both windows elapsed)
    // -----------------------------------------------------------------------

    @Test
    void checkIn_sessionClosed_throwsIllegalStateException() {
        var sessionId = UUID.randomUUID();
        var scope = LessonScope.builder().id(UUID.randomUUID()).build();

        // started 2 hours ago, windows only 1 second each → AWAITING_CONFIRMATION
        var session = CheckInSession.builder()
            .id(sessionId)
            .lessonScope(scope)
            .startedAt(Instant.now().minusSeconds(7200))
            .onTimeSeconds(1)
            .lateSeconds(1)
            .build();

        when(sessionRepository.findWithDetailsById(sessionId)).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> service.checkIn(sessionId, new StudentCheckInRequest(UUID.randomUUID())))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("closed");
    }

    // -----------------------------------------------------------------------
    // checkIn — student not in lesson audience
    // -----------------------------------------------------------------------

    @Test
    void checkIn_studentNotInAudience_throwsIllegalArgumentException() {
        var sessionId = UUID.randomUUID();
        var scope = LessonScope.builder().id(UUID.randomUUID()).build();

        // OPEN session: startedAt=now, onTimeSeconds=600 means window runs for 10 min
        var session = CheckInSession.builder()
            .id(sessionId)
            .lessonScope(scope)
            .startedAt(Instant.now())
            .onTimeSeconds(600)
            .lateSeconds(600)
            .build();

        when(sessionRepository.findWithDetailsById(sessionId)).thenReturn(Optional.of(session));

        // lessonStudentsApi returns a different student
        var otherStudent = Student.builder().id(UUID.randomUUID()).username("other").build();
        when(lessonStudentsApi.studentsOf(scope)).thenReturn(List.of(otherStudent));

        var foreignStudentId = UUID.randomUUID();
        assertThatThrownBy(() -> service.checkIn(sessionId, new StudentCheckInRequest(foreignStudentId)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("not part of this lesson audience");
    }

    // -----------------------------------------------------------------------
    // checkIn — cancelled session → closed state
    // -----------------------------------------------------------------------

    @Test
    void checkIn_cancelledSession_throwsIllegalStateException() {
        var sessionId = UUID.randomUUID();
        var scope = LessonScope.builder().id(UUID.randomUUID()).build();

        var session = CheckInSession.builder()
            .id(sessionId)
            .lessonScope(scope)
            .startedAt(Instant.now())
            .onTimeSeconds(600)
            .lateSeconds(600)
            .cancelledAt(Instant.now().minusSeconds(60))
            .build();

        when(sessionRepository.findWithDetailsById(sessionId)).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> service.checkIn(sessionId, new StudentCheckInRequest(UUID.randomUUID())))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("closed");
    }

    // -----------------------------------------------------------------------
    // checkIn — happy path: first check-in saves new record
    // -----------------------------------------------------------------------

    @Test
    void checkIn_firstCheckIn_savesNewRecord() {
        var sessionId = UUID.randomUUID();
        var studentId = UUID.randomUUID();
        var scope = LessonScope.builder().id(UUID.randomUUID()).build();

        // OPEN session
        var session = CheckInSession.builder()
            .id(sessionId)
            .lessonScope(scope)
            .startedAt(Instant.now())
            .onTimeSeconds(600)
            .lateSeconds(600)
            .build();

        when(sessionRepository.findWithDetailsById(sessionId)).thenReturn(Optional.of(session));

        var student = Student.builder().id(studentId).username("alice").build();
        when(lessonStudentsApi.studentsOf(scope)).thenReturn(List.of(student));
        when(studentRepository.getReferenceById(studentId)).thenReturn(student);

        // No existing record
        when(recordRepository.findBySessionIdAndStudentId(sessionId, studentId))
            .thenReturn(Optional.empty());

        var savedRecord = CheckInRecord.builder()
            .id(UUID.randomUUID())
            .session(session)
            .student(student)
            .status(CheckInRecordStatus.PRESENT)
            .checkedInAt(Instant.now())
            .build();
        when(recordRepository.save(any(CheckInRecord.class))).thenReturn(savedRecord);

        var expectedResponse = new CheckInRecordResponse(
            savedRecord.getId(), sessionId, studentId, CheckInRecordStatus.PRESENT, savedRecord.getCheckedInAt()
        );
        when(mapper.toResponse(savedRecord)).thenReturn(expectedResponse);

        var result = service.checkIn(sessionId, new StudentCheckInRequest(studentId));

        assertThat(result).isNotNull();
        assertThat(result.studentId()).isEqualTo(studentId);
        assertThat(result.sessionId()).isEqualTo(sessionId);
        assertThat(result.status()).isEqualTo(CheckInRecordStatus.PRESENT);
    }

    // -----------------------------------------------------------------------
    // checkIn — second check-in on same student does NOT downgrade status
    // -----------------------------------------------------------------------

    @Test
    void checkIn_secondCheckIn_doesNotDowngradeStatus() {
        var sessionId = UUID.randomUUID();
        var studentId = UUID.randomUUID();
        var scope = LessonScope.builder().id(UUID.randomUUID()).build();

        // OPEN session (not late window)
        var session = CheckInSession.builder()
            .id(sessionId)
            .lessonScope(scope)
            .startedAt(Instant.now())
            .onTimeSeconds(600)
            .lateSeconds(600)
            .build();

        when(sessionRepository.findWithDetailsById(sessionId)).thenReturn(Optional.of(session));

        var student = Student.builder().id(studentId).username("bob").build();
        when(lessonStudentsApi.studentsOf(scope)).thenReturn(List.of(student));

        // Existing record already has an id → service will not overwrite status
        var existingRecord = CheckInRecord.builder()
            .id(UUID.randomUUID())
            .session(session)
            .student(student)
            .status(CheckInRecordStatus.PRESENT)
            .checkedInAt(Instant.now().minusSeconds(30))
            .build();
        when(recordRepository.findBySessionIdAndStudentId(sessionId, studentId))
            .thenReturn(Optional.of(existingRecord));

        when(recordRepository.save(existingRecord)).thenReturn(existingRecord);

        var expectedResponse = new CheckInRecordResponse(
            existingRecord.getId(), sessionId, studentId, CheckInRecordStatus.PRESENT, existingRecord.getCheckedInAt()
        );
        when(mapper.toResponse(existingRecord)).thenReturn(expectedResponse);

        var result = service.checkIn(sessionId, new StudentCheckInRequest(studentId));

        // Status must remain PRESENT (not overwritten)
        assertThat(result.status()).isEqualTo(CheckInRecordStatus.PRESENT);
        assertThat(existingRecord.getStatus()).isEqualTo(CheckInRecordStatus.PRESENT);
    }
}
