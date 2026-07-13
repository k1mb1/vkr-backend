package com.github.k1mb1.vkr_backend.attendance.checkin.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.k1mb1.vkr_backend.attendance.checkin.domain.CheckInSessionEntity;
import com.github.k1mb1.vkr_backend.attendance.checkin.mapper.CheckInSessionMapper;
import com.github.k1mb1.vkr_backend.attendance.checkin.repository.CheckInRecordRepository;
import com.github.k1mb1.vkr_backend.attendance.checkin.repository.CheckInSessionRepository;
import com.github.k1mb1.vkr_backend.attendance.checkin.service.dto.response.CheckInSessionResponse;
import com.github.k1mb1.vkr_backend.attendance.repository.AttendanceLessonRepository;
import com.github.k1mb1.vkr_backend.attendance.repository.AttendanceLessonScopeRepository;
import com.github.k1mb1.vkr_backend.attendance.repository.AttendancePermissionRepository;
import com.github.k1mb1.vkr_backend.attendance.service.AttendanceService;
import com.github.k1mb1.vkr_backend.common.exception.ConflictException;
import com.github.k1mb1.vkr_backend.lesson.api.LessonStudentsApi;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CheckInSessionServiceConfirmCancelTest {

    @Mock
    CheckInSessionRepository sessionRepository;

    @Mock
    CheckInRecordRepository recordRepository;

    @Mock
    AttendanceLessonRepository lessonRepository;

    @Mock
    AttendanceLessonScopeRepository lessonScopeRepository;

    @Mock
    AttendancePermissionRepository permissionRepository;

    @Mock
    LessonStudentsApi lessonStudentsApi;

    @Mock
    CheckInSessionMapper mapper;

    @Mock
    AttendanceService attendanceService;

    @InjectMocks
    CheckInSessionService service;

    final UUID sessionId = UUID.randomUUID();
    final CheckInSessionEntity session = mock(CheckInSessionEntity.class);

    private void sessionLoaded() {
        when(sessionRepository.findWithDetailsById(sessionId)).thenReturn(Optional.of(session));
    }

    @Test
    void confirmRejectsAlreadyConfirmedSession() {
        sessionLoaded();
        when(session.getConfirmedAt()).thenReturn(Instant.now());

        assertThatThrownBy(() -> service.confirm(sessionId, null))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already confirmed");
        verify(sessionRepository, never()).save(any());
    }

    @Test
    void confirmRejectsCancelledSession() {
        sessionLoaded();
        when(session.getConfirmedAt()).thenReturn(null);
        when(session.getCancelledAt()).thenReturn(Instant.now());

        assertThatThrownBy(() -> service.confirm(sessionId, null))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("cancelled");
        verify(sessionRepository, never()).save(any());
    }

    @Test
    void cancelRejectsConfirmedSession() {
        sessionLoaded();
        when(session.getConfirmedAt()).thenReturn(Instant.now());

        assertThatThrownBy(() -> service.cancel(sessionId))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Cannot cancel a confirmed session");
        verify(sessionRepository, never()).save(any());
    }

    @Test
    void cancelIsIdempotentForAlreadyCancelledSession() {
        sessionLoaded();
        when(session.getConfirmedAt()).thenReturn(null);
        when(session.getCancelledAt()).thenReturn(Instant.now());
        lenient().when(mapper.toResponse(any(), any())).thenReturn(mock(CheckInSessionResponse.class));

        service.cancel(sessionId);

        // уже отменённая сессия повторно не сохраняется
        verify(sessionRepository, never()).save(any());
    }
}
