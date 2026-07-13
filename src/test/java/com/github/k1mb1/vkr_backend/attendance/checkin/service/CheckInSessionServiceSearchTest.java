package com.github.k1mb1.vkr_backend.attendance.checkin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.k1mb1.vkr_backend.attendance.api.AttendanceApi;
import com.github.k1mb1.vkr_backend.attendance.checkin.domain.CheckInSessionEntity;
import com.github.k1mb1.vkr_backend.attendance.checkin.domain.CheckInSessionState;
import com.github.k1mb1.vkr_backend.attendance.checkin.mapper.CheckInSessionMapper;
import com.github.k1mb1.vkr_backend.attendance.checkin.repository.CheckInRecordRepository;
import com.github.k1mb1.vkr_backend.attendance.checkin.repository.CheckInSessionRepository;
import com.github.k1mb1.vkr_backend.attendance.checkin.service.dto.response.PublicStudentResponse;
import com.github.k1mb1.vkr_backend.common.exception.ConflictException;
import com.github.k1mb1.vkr_backend.group.domain.StudentEntity;
import com.github.k1mb1.vkr_backend.lesson.api.LessonStudentsApi;
import com.github.k1mb1.vkr_backend.lesson.domain.LessonScopeEntity;
import com.github.k1mb1.vkr_backend.lesson.repository.LessonRepository;
import com.github.k1mb1.vkr_backend.lesson.repository.LessonScopeRepository;
import com.github.k1mb1.vkr_backend.subject.repository.TeacherSubjectPermissionRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CheckInSessionServiceSearchTest {

    static final String CODE = "ABC123";

    @Mock
    CheckInSessionRepository sessionRepository;

    @Mock
    CheckInRecordRepository recordRepository;

    @Mock
    LessonRepository lessonRepository;

    @Mock
    LessonScopeRepository lessonScopeRepository;

    @Mock
    TeacherSubjectPermissionRepository permissionRepository;

    @Mock
    LessonStudentsApi lessonStudentsApi;

    @Mock
    CheckInSessionMapper mapper;

    @Mock
    AttendanceApi attendanceApi;

    @InjectMocks
    CheckInSessionService service;

    final UUID sessionId = UUID.randomUUID();
    final LessonScopeEntity scope = mock(LessonScopeEntity.class);

    static StudentEntity student(String username) {
        return StudentEntity.builder().id(UUID.randomUUID()).username(username).build();
    }

    CheckInSessionEntity givenSessionWithStudents(List<StudentEntity> students) {
        var session = mock(CheckInSessionEntity.class);
        lenient().when(session.getCode()).thenReturn(CODE);
        lenient().when(session.getLessonScope()).thenReturn(scope);
        lenient().when(session.stateAt(any())).thenReturn(CheckInSessionState.OPEN);
        lenient().when(sessionRepository.findWithDetailsById(sessionId)).thenReturn(Optional.of(session));
        lenient().when(lessonStudentsApi.studentsOf(scope)).thenReturn(students);
        return session;
    }

    @Test
    void returnsOnlyMatchesBySurnameWithMaskedNameAndNoStatus() {
        var ivanov = student("Иванов Иван Иванович");
        givenSessionWithStudents(List.of(ivanov, student("Петров Пётр Петрович")));

        var result = service.searchStudents(sessionId, CODE, "иванов");

        assertThat(result)
                .containsExactly(PublicStudentResponse.builder()
                        .id(ivanov.getId())
                        .username("Иванов И. И.")
                        .build());
    }

    @Test
    void matchIsCaseInsensitiveAndTrimsQuery() {
        var petrov = student("Петров Пётр Петрович");
        givenSessionWithStudents(List.of(student("Иванов Иван"), petrov));

        var result = service.searchStudents(sessionId, CODE, "  ПЕТРОВ  ");

        assertThat(result).extracting(PublicStudentResponse::id).containsExactly(petrov.getId());
    }

    @Test
    void rejectsWrongCodeWithoutTouchingRoster() {
        givenSessionWithStudents(List.of(student("Иванов Иван Иванович")));

        assertThatThrownBy(() -> service.searchStudents(sessionId, "WRONG", "иванов"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("код");

        verify(lessonStudentsApi, never()).studentsOf(scope);
    }

    @Test
    void blankOrTooShortQueryReturnsEmptyAndDoesNotTouchRoster() {
        givenSessionWithStudents(List.of(student("Иванов Иван Иванович")));

        var result = service.searchStudents(sessionId, CODE, "и");

        assertThat(result).isEmpty();
        verify(lessonStudentsApi, never()).studentsOf(scope);
    }

    @Test
    void nullQueryReturnsEmpty() {
        givenSessionWithStudents(List.of(student("Иванов Иван Иванович")));

        assertThat(service.searchStudents(sessionId, CODE, null)).isEmpty();
    }

    @Test
    void returnsEmptyWhenSessionIsClosed() {
        var session = givenSessionWithStudents(List.of(student("Иванов Иван Иванович")));
        when(session.stateAt(any())).thenReturn(CheckInSessionState.CONFIRMED);

        assertThat(service.searchStudents(sessionId, CODE, "иванов")).isEmpty();
    }

    @Test
    void returnsEmptyWhenMoreThanOneMatch() {
        givenSessionWithStudents(List.of(student("Иванов Иван"), student("Иванов Пётр")));

        var result = service.searchStudents(sessionId, CODE, "иванов");

        assertThat(result).isEmpty();
    }

    @Test
    void returnsEmptyWhenNoMatches() {
        givenSessionWithStudents(List.of(student("Петров Пётр"), student("Сидоров Сидор")));

        var result = service.searchStudents(sessionId, CODE, "иванов");

        assertThat(result).isEmpty();
    }

    @Test
    void verifyCodePassesForValidCodeOnOpenSession() {
        givenSessionWithStudents(List.of());

        assertThatCode(() -> service.verifyCode(sessionId, "abc123")).doesNotThrowAnyException();
    }

    @Test
    void verifyCodeRejectsWrongCode() {
        givenSessionWithStudents(List.of());

        assertThatThrownBy(() -> service.verifyCode(sessionId, "WRONG")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void verifyCodeRejectsClosedSession() {
        var session = givenSessionWithStudents(List.of());
        when(session.stateAt(any())).thenReturn(CheckInSessionState.CONFIRMED);

        assertThatThrownBy(() -> service.verifyCode(sessionId, CODE)).isInstanceOf(ConflictException.class);
    }
}
