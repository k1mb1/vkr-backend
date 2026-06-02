package com.github.k1mb1.vkr_backend.attendance.checkin.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.github.k1mb1.vkr_backend.attendance.AttendanceApi;
import com.github.k1mb1.vkr_backend.attendance.checkin.domain.CheckInSession;
import com.github.k1mb1.vkr_backend.attendance.checkin.domain.CheckInSessionState;
import com.github.k1mb1.vkr_backend.attendance.checkin.web.requests.ConfirmCheckInRequest;
import com.github.k1mb1.vkr_backend.attendance.checkin.web.requests.StartCheckInRequest;
import com.github.k1mb1.vkr_backend.attendance.checkin.web.responses.CheckInSessionResponse;
import com.github.k1mb1.vkr_backend.attendance.domain.AttendanceStatus;
import com.github.k1mb1.vkr_backend.common.error.ResourceNotFoundException;
import com.github.k1mb1.vkr_backend.lesson.LessonStudentsApi;
import com.github.k1mb1.vkr_backend.lesson.domain.Lesson;
import com.github.k1mb1.vkr_backend.lesson.domain.LessonScope;
import com.github.k1mb1.vkr_backend.lesson.internal.LessonRepository;
import com.github.k1mb1.vkr_backend.lesson.internal.LessonScopeRepository;
import com.github.k1mb1.vkr_backend.student.domain.Student;
import com.github.k1mb1.vkr_backend.subject.domain.Subject;
import com.github.k1mb1.vkr_backend.subject.internal.TeacherSubjectPermissionRepository;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CheckInSessionServiceTest {

    @Mock CheckInSessionRepository sessionRepository;
    @Mock CheckInRecordRepository recordRepository;
    @Mock LessonRepository lessonRepository;
    @Mock LessonScopeRepository lessonScopeRepository;
    @Mock TeacherSubjectPermissionRepository permissionRepository;
    @Mock LessonStudentsApi lessonStudentsApi;
    @Mock CheckInSessionMapper mapper;
    @Mock AttendanceApi attendanceApi;

    @InjectMocks CheckInSessionService service;

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private CheckInSession openSession(UUID sessionId, LessonScope scope) {
        return CheckInSession.builder()
            .id(sessionId)
            .lessonScope(scope)
            .startedAt(Instant.now())
            .onTimeSeconds(600)
            .lateSeconds(600)
            .build();
    }

    private LessonScope scopeWithLesson() {
        var subject = Subject.builder().id(UUID.randomUUID()).name("Math").build();
        var lesson = Lesson.builder().id(UUID.randomUUID()).subject(subject).build();
        return LessonScope.builder().id(UUID.randomUUID()).lesson(lesson).build();
    }

    private CheckInSessionResponse stubResponse(CheckInSession session) {
        return new CheckInSessionResponse(
            session.getId(),
            session.getLessonScope().getLesson().getId(),
            session.getLessonScope().getId(),
            false,
            Collections.emptyList(),
            session.getStartedAt(),
            session.getOnTimeSeconds(),
            session.getLateSeconds(),
            session.onTimeEndsAt(),
            session.lateEndsAt(),
            session.getConfirmedAt(),
            session.getCancelledAt(),
            session.stateAt(Instant.now())
        );
    }

    // -----------------------------------------------------------------------
    // start — lesson scope not found
    // -----------------------------------------------------------------------

    @Test
    void start_lessonScopeNotFound_throwsResourceNotFoundException() {
        var scopeId = UUID.randomUUID();
        when(lessonScopeRepository.findWithDetailsById(scopeId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.start(new StartCheckInRequest(scopeId, 600, 300)))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("LessonScope");
    }

    // -----------------------------------------------------------------------
    // start — active session already exists
    // -----------------------------------------------------------------------

    @Test
    void start_activeSessionAlreadyExists_throwsIllegalStateException() {
        var scope = scopeWithLesson();
        when(lessonScopeRepository.findWithDetailsById(scope.getId())).thenReturn(Optional.of(scope));

        var existingSession = openSession(UUID.randomUUID(), scope);
        when(sessionRepository.findByLessonScopeIdAndConfirmedAtIsNullAndCancelledAtIsNull(scope.getId()))
            .thenReturn(Optional.of(existingSession));

        assertThatThrownBy(() -> service.start(new StartCheckInRequest(scope.getId(), 600, 300)))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Active check-in session already exists");
    }

    // -----------------------------------------------------------------------
    // start — happy path
    // -----------------------------------------------------------------------

    @Test
    void start_happyPath_returnsSessionResponse() {
        var scope = scopeWithLesson();
        when(lessonScopeRepository.findWithDetailsById(scope.getId())).thenReturn(Optional.of(scope));
        when(sessionRepository.findByLessonScopeIdAndConfirmedAtIsNullAndCancelledAtIsNull(scope.getId()))
            .thenReturn(Optional.empty());

        var saved = openSession(UUID.randomUUID(), scope);
        when(sessionRepository.save(any(CheckInSession.class))).thenReturn(saved);

        var response = stubResponse(saved);
        when(mapper.toResponse(any(CheckInSession.class), any(Instant.class))).thenReturn(response);

        var result = service.start(new StartCheckInRequest(scope.getId(), 600, 300));

        assertThat(result).isNotNull();
        assertThat(result.lessonScopeId()).isEqualTo(scope.getId());
    }

    // -----------------------------------------------------------------------
    // get — session not found
    // -----------------------------------------------------------------------

    @Test
    void get_sessionNotFound_throwsResourceNotFoundException() {
        var sessionId = UUID.randomUUID();
        when(sessionRepository.findWithDetailsById(sessionId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(sessionId))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("CheckInSession");
    }

    // -----------------------------------------------------------------------
    // get — happy path
    // -----------------------------------------------------------------------

    @Test
    void get_happyPath_returnsSessionResponse() {
        var scope = scopeWithLesson();
        var sessionId = UUID.randomUUID();
        var session = openSession(sessionId, scope);

        when(sessionRepository.findWithDetailsById(sessionId)).thenReturn(Optional.of(session));
        var response = stubResponse(session);
        when(mapper.toResponse(any(CheckInSession.class), any(Instant.class))).thenReturn(response);

        var result = service.get(sessionId);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(sessionId);
    }

    // -----------------------------------------------------------------------
    // confirm — already confirmed
    // -----------------------------------------------------------------------

    @Test
    void confirm_alreadyConfirmed_throwsIllegalStateException() {
        var scope = scopeWithLesson();
        var sessionId = UUID.randomUUID();
        var session = openSession(sessionId, scope);
        session.setConfirmedAt(Instant.now().minusSeconds(60));

        when(sessionRepository.findWithDetailsById(sessionId)).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> service.confirm(sessionId, null))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("already confirmed");
    }

    // -----------------------------------------------------------------------
    // confirm — session is cancelled
    // -----------------------------------------------------------------------

    @Test
    void confirm_cancelledSession_throwsIllegalStateException() {
        var scope = scopeWithLesson();
        var sessionId = UUID.randomUUID();
        var session = openSession(sessionId, scope);
        session.setCancelledAt(Instant.now().minusSeconds(60));

        when(sessionRepository.findWithDetailsById(sessionId)).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> service.confirm(sessionId, null))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("cancelled");
    }

    // -----------------------------------------------------------------------
    // confirm — override references student not in audience
    // -----------------------------------------------------------------------

    @Test
    void confirm_overrideStudentNotInAudience_throwsIllegalArgumentException() {
        var scope = scopeWithLesson();
        var sessionId = UUID.randomUUID();
        var session = openSession(sessionId, scope);

        when(sessionRepository.findWithDetailsById(sessionId)).thenReturn(Optional.of(session));

        // Only one real student in audience
        var realStudent = Student.builder().id(UUID.randomUUID()).username("alice").build();
        when(lessonStudentsApi.studentsOf(scope)).thenReturn(List.of(realStudent));
        when(recordRepository.findBySessionId(sessionId)).thenReturn(List.of());

        // Override referencing a foreign student id
        var foreignId = UUID.randomUUID();
        var override = new ConfirmCheckInRequest.Override(foreignId, AttendanceStatus.PRESENT, null);
        var request = new ConfirmCheckInRequest(List.of(override));

        assertThatThrownBy(() -> service.confirm(sessionId, request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("not in lesson audience");
    }

    // -----------------------------------------------------------------------
    // confirm — happy path (null request, no overrides)
    // -----------------------------------------------------------------------

    @Test
    void confirm_happyPath_setsConfirmedAtAndReturnsResponse() {
        var scope = scopeWithLesson();
        var sessionId = UUID.randomUUID();
        var session = openSession(sessionId, scope);

        when(sessionRepository.findWithDetailsById(sessionId)).thenReturn(Optional.of(session));

        var student = Student.builder().id(UUID.randomUUID()).username("alice").build();
        when(lessonStudentsApi.studentsOf(scope)).thenReturn(List.of(student));
        when(recordRepository.findBySessionId(sessionId)).thenReturn(List.of());
        when(attendanceApi.upsertAll(any())).thenReturn(List.of());

        when(sessionRepository.save(session)).thenReturn(session);

        var response = new CheckInSessionResponse(
            sessionId, scope.getLesson().getId(), scope.getId(), false,
            Collections.emptyList(), session.getStartedAt(),
            session.getOnTimeSeconds(), session.getLateSeconds(),
            session.onTimeEndsAt(), session.lateEndsAt(),
            Instant.now(), null, CheckInSessionState.CONFIRMED
        );
        when(mapper.toResponse(any(CheckInSession.class), any(Instant.class))).thenReturn(response);

        var result = service.confirm(sessionId, null);

        assertThat(result).isNotNull();
        assertThat(session.getConfirmedAt()).isNotNull();
    }

    // -----------------------------------------------------------------------
    // cancel — cannot cancel a confirmed session
    // -----------------------------------------------------------------------

    @Test
    void cancel_alreadyConfirmed_throwsIllegalStateException() {
        var scope = scopeWithLesson();
        var sessionId = UUID.randomUUID();
        var session = openSession(sessionId, scope);
        session.setConfirmedAt(Instant.now().minusSeconds(60));

        when(sessionRepository.findWithDetailsById(sessionId)).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> service.cancel(sessionId))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Cannot cancel a confirmed session");
    }

    // -----------------------------------------------------------------------
    // cancel — happy path: sets cancelledAt
    // -----------------------------------------------------------------------

    @Test
    void cancel_happyPath_setsCancelledAt() {
        var scope = scopeWithLesson();
        var sessionId = UUID.randomUUID();
        var session = openSession(sessionId, scope);

        when(sessionRepository.findWithDetailsById(sessionId)).thenReturn(Optional.of(session));
        when(sessionRepository.save(session)).thenReturn(session);

        var response = new CheckInSessionResponse(
            sessionId, scope.getLesson().getId(), scope.getId(), false,
            Collections.emptyList(), session.getStartedAt(),
            session.getOnTimeSeconds(), session.getLateSeconds(),
            session.onTimeEndsAt(), session.lateEndsAt(),
            null, Instant.now(), CheckInSessionState.CANCELLED
        );
        when(mapper.toResponse(any(CheckInSession.class), any(Instant.class))).thenReturn(response);

        var result = service.cancel(sessionId);

        assertThat(result).isNotNull();
        assertThat(session.getCancelledAt()).isNotNull();
    }

    // -----------------------------------------------------------------------
    // cancel — idempotent: already cancelled session does not throw
    // -----------------------------------------------------------------------

    @Test
    void cancel_alreadyCancelled_isIdempotent() {
        var scope = scopeWithLesson();
        var sessionId = UUID.randomUUID();
        var session = openSession(sessionId, scope);
        session.setCancelledAt(Instant.now().minusSeconds(10));

        when(sessionRepository.findWithDetailsById(sessionId)).thenReturn(Optional.of(session));

        var response = new CheckInSessionResponse(
            sessionId, scope.getLesson().getId(), scope.getId(), false,
            Collections.emptyList(), session.getStartedAt(),
            session.getOnTimeSeconds(), session.getLateSeconds(),
            session.onTimeEndsAt(), session.lateEndsAt(),
            null, session.getCancelledAt(), CheckInSessionState.CANCELLED
        );
        when(mapper.toResponse(any(CheckInSession.class), any(Instant.class))).thenReturn(response);

        // Should not throw
        var result = service.cancel(sessionId);

        assertThat(result).isNotNull();
    }
}
