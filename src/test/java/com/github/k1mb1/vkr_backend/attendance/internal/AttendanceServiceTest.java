package com.github.k1mb1.vkr_backend.attendance.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.k1mb1.vkr_backend.attendance.domain.Attendance;
import com.github.k1mb1.vkr_backend.attendance.domain.AttendanceStatus;
import com.github.k1mb1.vkr_backend.attendance.web.requests.BulkUpsertAttendanceRequest;
import com.github.k1mb1.vkr_backend.attendance.web.requests.UpsertAttendanceRequest;
import com.github.k1mb1.vkr_backend.attendance.web.responses.AttendanceCellResponse;
import com.github.k1mb1.vkr_backend.common.error.ResourceNotFoundException;
import com.github.k1mb1.vkr_backend.lesson.domain.LessonScope;
import com.github.k1mb1.vkr_backend.lesson.internal.LessonResolver;
import com.github.k1mb1.vkr_backend.lesson.internal.LessonScopeRepository;
import com.github.k1mb1.vkr_backend.student.domain.Student;
import com.github.k1mb1.vkr_backend.student.internal.StudentRepository;
import com.github.k1mb1.vkr_backend.subject.internal.SubjectMapper;
import com.github.k1mb1.vkr_backend.subject.internal.TeacherSubjectPermissionRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock
    AttendanceRepository attendanceRepository;

    @Mock
    AttendanceMapper attendanceMapper;

    @Mock
    TeacherSubjectPermissionRepository permissionRepository;

    @Mock
    LessonResolver lessonResolver;

    @Mock
    LessonScopeRepository lessonScopeRepository;

    @Mock
    StudentRepository studentRepository;

    @Mock
    com.github.k1mb1.vkr_backend.lesson.LessonStudentsApi lessonStudentsApi;

    @Mock
    SubjectMapper subjectMapper;

    @InjectMocks
    AttendanceService service;

    private UpsertAttendanceRequest item(UUID studentId, UUID scopeId, AttendanceStatus status) {
        return UpsertAttendanceRequest.builder()
            .studentId(studentId).lessonScopeId(scopeId).status(status).build();
    }

    // ---- upsertAll ----

    @Test
    void upsertAllRejectsDuplicatePair() {
        var studentId = UUID.randomUUID();
        var scopeId = UUID.randomUUID();
        var request = new BulkUpsertAttendanceRequest(List.of(
            item(studentId, scopeId, AttendanceStatus.PRESENT),
            item(studentId, scopeId, AttendanceStatus.LATE)
        ));

        assertThatThrownBy(() -> service.upsertAll(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Duplicate");
    }

    @Test
    void upsertAllCreatesNewAndUpdatesExisting() {
        var s1 = UUID.randomUUID();
        var s2 = UUID.randomUUID();
        var scopeId = UUID.randomUUID();
        var student2 = Student.builder().id(s2).username("B").build();
        var scope = LessonScope.builder().id(scopeId).build();

        // существующая ячейка для s2; для s1 — новая
        var existing = Attendance.builder()
            .id(UUID.randomUUID())
            .student(student2)
            .lessonScope(scope)
            .status(AttendanceStatus.ABSENT)
            .build();
        when(attendanceRepository.findByLessonScopeIdInAndStudentIdIn(any(), any()))
            .thenReturn(List.of(existing));
        when(studentRepository.getReferenceById(s1))
            .thenReturn(Student.builder().id(s1).username("A").build());
        when(lessonScopeRepository.getReferenceById(scopeId)).thenReturn(scope);
        when(attendanceRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(attendanceMapper.toCell(any())).thenReturn(mock(AttendanceCellResponse.class));

        var request = new BulkUpsertAttendanceRequest(List.of(
            item(s1, scopeId, AttendanceStatus.PRESENT),
            item(s2, scopeId, AttendanceStatus.LATE)
        ));

        service.upsertAll(request);

        var captor = ArgumentCaptor.forClass(List.class);
        verify(attendanceRepository).saveAll(captor.capture());
        List<Attendance> saved = captor.getValue();
        assertThat(saved).hasSize(2);
        // существующая обновлена на LATE (а не пересоздана)
        assertThat(existing.getStatus()).isEqualTo(AttendanceStatus.LATE);
    }

    // ---- summarize ----

    @Test
    void summarizeReturnsEmptyForEmptyInputs() {
        assertThat(service.summarize(List.of(), List.of(UUID.randomUUID()))).isEmpty();
        assertThat(service.summarize(List.of(UUID.randomUUID()), List.of())).isEmpty();
    }

    @Test
    void summarizeCountsPerStudentByStatus() {
        var studentId = UUID.randomUUID();
        var student = Student.builder().id(studentId).username("A").build();
        var scopeId = UUID.randomUUID();
        var rows = List.of(
            Attendance.builder().id(UUID.randomUUID()).student(student).status(AttendanceStatus.PRESENT).build(),
            Attendance.builder().id(UUID.randomUUID()).student(student).status(AttendanceStatus.PRESENT).build(),
            Attendance.builder().id(UUID.randomUUID()).student(student).status(AttendanceStatus.LATE).build(),
            Attendance.builder().id(UUID.randomUUID()).student(student).status(AttendanceStatus.ABSENT).build(),
            Attendance.builder().id(UUID.randomUUID()).student(student).status(AttendanceStatus.EXCUSED).build()
        );
        when(attendanceRepository.findByLessonScopeIdInAndStudentIdIn(any(), any())).thenReturn(rows);

        var result = service.summarize(List.of(scopeId), List.of(studentId));

        var summary = result.get(studentId);
        assertThat(summary.present()).isEqualTo(2);
        assertThat(summary.late()).isEqualTo(1);
        assertThat(summary.absent()).isEqualTo(1);
        assertThat(summary.excused()).isEqualTo(1);
    }

    // ---- getAttendanceTable ----

    @Test
    void getAttendanceTableThrowsWhenPermissionMissing() {
        var permissionId = UUID.randomUUID();
        when(permissionRepository.findWithDetailsById(permissionId)).thenReturn(Optional.empty());

        var filter = new com.github.k1mb1.vkr_backend.attendance.web.filters.AttendanceFilter(
            permissionId, null, null);

        assertThatThrownBy(() -> service.getAttendanceTable(filter))
            .isInstanceOf(ResourceNotFoundException.class);
    }
}
