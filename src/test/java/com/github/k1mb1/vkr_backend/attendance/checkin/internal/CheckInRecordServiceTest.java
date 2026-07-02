package com.github.k1mb1.vkr_backend.attendance.checkin.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.k1mb1.vkr_backend.attendance.checkin.domain.CheckInRecord;
import com.github.k1mb1.vkr_backend.attendance.checkin.domain.CheckInRecordStatus;
import com.github.k1mb1.vkr_backend.attendance.checkin.domain.CheckInSession;
import com.github.k1mb1.vkr_backend.attendance.checkin.domain.CheckInSessionState;
import com.github.k1mb1.vkr_backend.attendance.checkin.web.requests.StudentCheckInRequest;
import com.github.k1mb1.vkr_backend.common.error.ConflictException;
import com.github.k1mb1.vkr_backend.lesson.LessonStudentsApi;
import com.github.k1mb1.vkr_backend.lesson.domain.LessonScope;
import com.github.k1mb1.vkr_backend.student.domain.Student;
import com.github.k1mb1.vkr_backend.student.internal.StudentRepository;
import java.time.Instant;
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
    StudentRepository studentRepository;

    @Mock
    LessonStudentsApi lessonStudentsApi;

    @InjectMocks
    CheckInRecordService service;

    final UUID sessionId = UUID.randomUUID();
    final UUID studentId = UUID.randomUUID();
    final LessonScope scope = mock(LessonScope.class);
    final CheckInSession session = mock(CheckInSession.class);

    @BeforeEach
    void setUp() {
        var student = Student.builder()
            .id(studentId)
            .username("Иванов Иван")
            .build();
        lenient().when(session.getLessonScope()).thenReturn(scope);
        lenient().when(session.getCode()).thenReturn("ABC123");
        lenient()
            .when(session.stateAt(any()))
            .thenReturn(CheckInSessionState.OPEN);
        lenient()
            .when(session.statusForCheckInAt(any()))
            .thenReturn(CheckInRecordStatus.PRESENT);
        lenient()
            .when(sessionRepository.findWithDetailsById(sessionId))
            .thenReturn(Optional.of(session));
        lenient()
            .when(lessonStudentsApi.studentsOf(scope))
            .thenReturn(List.of(student));
        lenient()
            .when(
                recordRepository.findBySessionIdAndStudentId(
                    sessionId,
                    studentId
                )
            )
            .thenReturn(Optional.empty());
        lenient()
            .when(studentRepository.getReferenceById(studentId))
            .thenReturn(student);
        lenient()
            .when(recordRepository.save(any()))
            .thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void checksInWithValidCodeAndReturnsOwnStatusOnly() {
        var response = service.checkIn(
            sessionId,
            StudentCheckInRequest.builder()
                .studentId(studentId)
                .code("abc123")
                .build()
        );

        assertThat(response.status()).isEqualTo(CheckInRecordStatus.PRESENT);
        assertThat(response.checkedInAt()).isNotNull();
        verify(recordRepository).save(any(CheckInRecord.class));
    }

    @Test
    void rejectsWrongCodeWithoutCheckingStudentScope() {
        assertThatThrownBy(() ->
            service.checkIn(
                sessionId,
                StudentCheckInRequest.builder()
                    .studentId(studentId)
                    .code("WRONG")
                    .build()
            )
        )
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("код");

        // Код проверяется раньше принадлежности студента и записи — нет утечки/записи.
        verify(lessonStudentsApi, never()).studentsOf(any(LessonScope.class));
        verify(recordRepository, never()).save(any());
    }

    @Test
    void rejectsCheckInWhenSessionClosed() {
        when(session.stateAt(any())).thenReturn(CheckInSessionState.CONFIRMED);

        assertThatThrownBy(() ->
            service.checkIn(
                sessionId,
                StudentCheckInRequest.builder()
                    .studentId(studentId)
                    .code("ABC123")
                    .build()
            )
        ).isInstanceOf(ConflictException.class);

        verify(recordRepository, never()).save(any());
    }
}
