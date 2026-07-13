package com.github.k1mb1.vkr_backend.attendance.checkin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.k1mb1.vkr_backend.attendance.api.AttendanceApi;
import com.github.k1mb1.vkr_backend.attendance.checkin.domain.CheckInSessionEntity;
import com.github.k1mb1.vkr_backend.attendance.checkin.mapper.CheckInSessionMapper;
import com.github.k1mb1.vkr_backend.attendance.checkin.repository.CheckInRecordRepository;
import com.github.k1mb1.vkr_backend.attendance.checkin.repository.CheckInSessionRepository;
import com.github.k1mb1.vkr_backend.attendance.checkin.service.dto.request.StartCheckInRequest;
import com.github.k1mb1.vkr_backend.lesson.api.LessonStudentsApi;
import com.github.k1mb1.vkr_backend.lesson.domain.LessonEntity;
import com.github.k1mb1.vkr_backend.lesson.domain.LessonScopeEntity;
import com.github.k1mb1.vkr_backend.lesson.repository.LessonRepository;
import com.github.k1mb1.vkr_backend.lesson.repository.LessonScopeRepository;
import com.github.k1mb1.vkr_backend.subject.domain.CheckInPolicyEntity;
import com.github.k1mb1.vkr_backend.subject.domain.SubjectEntity;
import com.github.k1mb1.vkr_backend.subject.repository.TeacherSubjectPermissionRepository;
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
    final LessonScopeEntity scope = mock(LessonScopeEntity.class);
    final SubjectEntity subject = mock(SubjectEntity.class);

    @BeforeEach
    void setUp() {
        var lesson = mock(LessonEntity.class);
        lenient().when(scope.getLesson()).thenReturn(lesson);
        lenient().when(lesson.getSubject()).thenReturn(subject);
        lenient().when(lessonScopeRepository.findWithDetailsById(scopeId)).thenReturn(Optional.of(scope));
        lenient()
                .when(sessionRepository.findByLessonScopeIdAndConfirmedAtIsNullAndCancelledAtIsNull(any()))
                .thenReturn(Optional.empty());
        lenient().when(sessionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    private CheckInSessionEntity savedSession() {
        var captor = ArgumentCaptor.forClass(CheckInSessionEntity.class);
        verify(sessionRepository).save(captor.capture());
        return captor.getValue();
    }

    @Test
    void usesPolicyWindowsIgnoringRequestWhenPolicyEnabled() {
        when(subject.getCheckInPolicy())
                .thenReturn(CheckInPolicyEntity.builder()
                        .enabled(true)
                        .onTimeSeconds(300)
                        .lateSeconds(120)
                        .build());

        service.start(StartCheckInRequest.builder()
                .lessonScopeId(scopeId)
                .onTimeSeconds(999)
                .lateSeconds(999)
                .build());

        var saved = savedSession();
        assertThat(saved.getOnTimeSeconds()).isEqualTo(300);
        assertThat(saved.getLateSeconds()).isEqualTo(120);
        assertThat(saved.getCode()).isNotBlank();
    }

    @Test
    void usesRequestWindowsWhenNoPolicy() {
        when(subject.getCheckInPolicy())
                .thenReturn(CheckInPolicyEntity.builder().enabled(false).build());

        service.start(StartCheckInRequest.builder()
                .lessonScopeId(scopeId)
                .onTimeSeconds(600)
                .lateSeconds(300)
                .build());

        var saved = savedSession();
        assertThat(saved.getOnTimeSeconds()).isEqualTo(600);
        assertThat(saved.getLateSeconds()).isEqualTo(300);
    }

    @Test
    void rejectsMissingWindowsWhenNoPolicy() {
        when(subject.getCheckInPolicy())
                .thenReturn(CheckInPolicyEntity.builder().enabled(false).build());

        assertThatThrownBy(() -> service.start(StartCheckInRequest.builder()
                        .lessonScopeId(scopeId)
                        .onTimeSeconds(null)
                        .lateSeconds(null)
                        .build()))
                .isInstanceOf(IllegalArgumentException.class);

        verify(sessionRepository, never()).save(any());
    }
}
