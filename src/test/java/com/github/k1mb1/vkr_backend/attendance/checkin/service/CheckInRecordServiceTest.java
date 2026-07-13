package com.github.k1mb1.vkr_backend.attendance.checkin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.k1mb1.vkr_backend.attendance.checkin.CheckInRecordStatus;
import com.github.k1mb1.vkr_backend.attendance.checkin.CheckInSessionState;
import com.github.k1mb1.vkr_backend.attendance.checkin.domain.CheckInRecordEntity;
import com.github.k1mb1.vkr_backend.attendance.checkin.domain.CheckInSessionEntity;
import com.github.k1mb1.vkr_backend.attendance.checkin.repository.CheckInRecordRepository;
import com.github.k1mb1.vkr_backend.attendance.checkin.repository.CheckInSessionRepository;
import com.github.k1mb1.vkr_backend.attendance.checkin.service.dto.request.StudentCheckInRequest;
import com.github.k1mb1.vkr_backend.attendance.repository.AttendanceStudentRefRepository;
import com.github.k1mb1.vkr_backend.common.exception.ConflictException;
import com.github.k1mb1.vkr_backend.group.domain.StudentEntity;
import com.github.k1mb1.vkr_backend.lesson.api.LessonStudentResponse;
import com.github.k1mb1.vkr_backend.lesson.api.LessonStudentsApi;
import com.github.k1mb1.vkr_backend.lesson.domain.LessonScopeEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CheckInRecordServiceTest {

    @Mock
    CheckInSessionRepository sessionRepository;

    @Mock
    CheckInRecordRepository recordRepository;

    @Mock
    AttendanceStudentRefRepository studentRefRepository;

    @Mock
    LessonStudentsApi lessonStudentsApi;

    @InjectMocks
    CheckInRecordService service;

    final UUID sessionId = UUID.randomUUID();
    final UUID studentId = UUID.randomUUID();
    final LessonScopeEntity scope = mock(LessonScopeEntity.class);
    final CheckInSessionEntity session = mock(CheckInSessionEntity.class);

    @BeforeEach
    void setUp() {
        var student =
                StudentEntity.builder().id(studentId).username("Иванов Иван").build();
        var rosterStudent = new LessonStudentResponse(studentId, "Иванов Иван", UUID.randomUUID(), "Гр-1", null, null);
        lenient().when(session.getLessonScope()).thenReturn(scope);
        lenient().when(session.getCode()).thenReturn("ABC123");
        lenient().when(session.stateAt(any())).thenReturn(CheckInSessionState.OPEN);
        lenient().when(session.statusForCheckInAt(any())).thenReturn(CheckInRecordStatus.PRESENT);
        lenient().when(sessionRepository.findWithDetailsById(sessionId)).thenReturn(Optional.of(session));
        lenient().when(scope.getId()).thenReturn(UUID.randomUUID());
        lenient().when(lessonStudentsApi.studentsOfScope(scope.getId())).thenReturn(List.of(rosterStudent));
        lenient()
                .when(recordRepository.findBySessionIdAndStudentId(sessionId, studentId))
                .thenReturn(Optional.empty());
        lenient().when(studentRefRepository.getReferenceById(studentId)).thenReturn(student);
        lenient().when(recordRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void checksInWithValidCodeAndReturnsOwnStatusOnly() {
        var response = service.checkIn(
                sessionId,
                StudentCheckInRequest.builder()
                        .studentId(studentId)
                        .code("abc123")
                        .build());

        assertThat(response.status()).isEqualTo(CheckInRecordStatus.PRESENT);
        assertThat(response.checkedInAt()).isNotNull();
        verify(recordRepository).save(any(CheckInRecordEntity.class));
    }

    @Test
    void rejectsWrongCodeWithoutCheckingStudentScope() {
        assertThatThrownBy(() -> service.checkIn(
                        sessionId,
                        StudentCheckInRequest.builder()
                                .studentId(studentId)
                                .code("WRONG")
                                .build()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("код");

        // Код проверяется раньше принадлежности студента и записи — нет утечки/записи.
        verify(lessonStudentsApi, never()).studentsOfScope(any(UUID.class));
        verify(recordRepository, never()).save(any());
    }

    @Test
    void rejectsCheckInWhenSessionClosed() {
        when(session.stateAt(any())).thenReturn(CheckInSessionState.CONFIRMED);

        assertThatThrownBy(() -> service.checkIn(
                        sessionId,
                        StudentCheckInRequest.builder()
                                .studentId(studentId)
                                .code("ABC123")
                                .build()))
                .isInstanceOf(ConflictException.class);

        verify(recordRepository, never()).save(any());
    }
}
