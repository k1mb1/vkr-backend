package com.github.k1mb1.vkr_backend.attendance.checkin.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.k1mb1.vkr_backend.attendance.AttendanceApi;
import com.github.k1mb1.vkr_backend.attendance.checkin.domain.CheckInSession;
import com.github.k1mb1.vkr_backend.attendance.checkin.web.requests.StartCheckInRequest;
import com.github.k1mb1.vkr_backend.lesson.LessonStudentsApi;
import com.github.k1mb1.vkr_backend.lesson.domain.Lesson;
import com.github.k1mb1.vkr_backend.lesson.domain.LessonScope;
import com.github.k1mb1.vkr_backend.lesson.internal.LessonRepository;
import com.github.k1mb1.vkr_backend.lesson.internal.LessonScopeRepository;
import com.github.k1mb1.vkr_backend.subject.domain.CheckInPolicy;
import com.github.k1mb1.vkr_backend.subject.domain.Subject;
import com.github.k1mb1.vkr_backend.subject.internal.TeacherSubjectPermissionRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CheckInSessionServiceStartTest {

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

    final UUID scopeId = UUID.randomUUID();
    final LessonScope scope = mock(LessonScope.class);
    final Subject subject = mock(Subject.class);

    @BeforeEach
    void setUp() {
        var lesson = mock(Lesson.class);
        lenient().when(scope.getLesson()).thenReturn(lesson);
        lenient().when(lesson.getSubject()).thenReturn(subject);
        lenient()
            .when(lessonScopeRepository.findWithDetailsById(scopeId))
            .thenReturn(Optional.of(scope));
        lenient()
            .when(
                sessionRepository
                    .findByLessonScopeIdAndConfirmedAtIsNullAndCancelledAtIsNull(any())
            )
            .thenReturn(Optional.empty());
        lenient()
            .when(sessionRepository.save(any()))
            .thenAnswer(inv -> inv.getArgument(0));
    }

    private CheckInSession savedSession() {
        var captor = ArgumentCaptor.forClass(CheckInSession.class);
        verify(sessionRepository).save(captor.capture());
        return captor.getValue();
    }

    @Test
    void usesPolicyWindowsIgnoringRequestWhenPolicyEnabled() {
        when(subject.getCheckInPolicy()).thenReturn(
            CheckInPolicy.builder().enabled(true).onTimeSeconds(300).lateSeconds(120).build()
        );

        service.start(new StartCheckInRequest(scopeId, 999, 999));

        var saved = savedSession();
        assertThat(saved.getOnTimeSeconds()).isEqualTo(300);
        assertThat(saved.getLateSeconds()).isEqualTo(120);
        assertThat(saved.getCode()).isNotBlank();
    }

    @Test
    void usesRequestWindowsWhenNoPolicy() {
        when(subject.getCheckInPolicy()).thenReturn(
            CheckInPolicy.builder().enabled(false).build()
        );

        service.start(new StartCheckInRequest(scopeId, 600, 300));

        var saved = savedSession();
        assertThat(saved.getOnTimeSeconds()).isEqualTo(600);
        assertThat(saved.getLateSeconds()).isEqualTo(300);
    }

    @Test
    void rejectsMissingWindowsWhenNoPolicy() {
        when(subject.getCheckInPolicy()).thenReturn(
            CheckInPolicy.builder().enabled(false).build()
        );

        assertThatThrownBy(() ->
            service.start(new StartCheckInRequest(scopeId, null, null))
        ).isInstanceOf(IllegalArgumentException.class);

        verify(sessionRepository, never()).save(any());
    }
}
