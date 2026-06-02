package com.github.k1mb1.vkr_backend.attendance.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

import com.github.k1mb1.vkr_backend.attendance.domain.Attendance;
import com.github.k1mb1.vkr_backend.attendance.domain.AttendanceStatus;
import com.github.k1mb1.vkr_backend.attendance.web.requests.BulkUpsertAttendanceRequest;
import com.github.k1mb1.vkr_backend.attendance.web.requests.UpsertAttendanceRequest;
import com.github.k1mb1.vkr_backend.attendance.web.responses.AttendanceCellResponse;
import com.github.k1mb1.vkr_backend.lesson.LessonStudentsApi;
import com.github.k1mb1.vkr_backend.lesson.internal.LessonRepository;
import com.github.k1mb1.vkr_backend.lesson.internal.LessonScopeRepository;
import com.github.k1mb1.vkr_backend.student.internal.StudentRepository;
import com.github.k1mb1.vkr_backend.subject.internal.TeacherSubjectPermissionRepository;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceUpsertTest {

    @Mock AttendanceRepository attendanceRepository;
    @Mock AttendanceMapper attendanceMapper;
    @Mock TeacherSubjectPermissionRepository permissionRepository;
    @Mock LessonRepository lessonRepository;
    @Mock LessonScopeRepository lessonScopeRepository;
    @Mock StudentRepository studentRepository;
    @Mock LessonStudentsApi lessonStudentsApi;

    @InjectMocks AttendanceService service;

    // -----------------------------------------------------------------------
    // upsertAll — duplicate (studentId, lessonScopeId) in request
    // -----------------------------------------------------------------------

    @Test
    void upsertAll_duplicatePairInRequest_throwsIllegalArgumentException() {
        var studentId = UUID.randomUUID();
        var scopeId = UUID.randomUUID();

        var item = new UpsertAttendanceRequest(studentId, scopeId, AttendanceStatus.PRESENT, null);
        var duplicate = new UpsertAttendanceRequest(studentId, scopeId, AttendanceStatus.ABSENT, null);

        var request = new BulkUpsertAttendanceRequest(List.of(item, duplicate));

        assertThatThrownBy(() -> service.upsertAll(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining(studentId.toString())
            .hasMessageContaining(scopeId.toString());
    }

    // -----------------------------------------------------------------------
    // upsertAll — happy path: new records are saved and mapped
    // -----------------------------------------------------------------------

    @Test
    void upsertAll_newItems_savesAndReturnsMappedCells() {
        var studentId = UUID.randomUUID();
        var scopeId = UUID.randomUUID();

        var item = new UpsertAttendanceRequest(studentId, scopeId, AttendanceStatus.PRESENT, "good");
        var request = new BulkUpsertAttendanceRequest(List.of(item));

        // No existing attendance found
        when(attendanceRepository.findByLessonScopeIdInAndStudentIdIn(anyList(), anyList()))
            .thenReturn(List.of());

        // getReferenceById returns a proxy (enough for builder logic)
        when(studentRepository.getReferenceById(studentId))
            .thenReturn(com.github.k1mb1.vkr_backend.student.domain.Student.builder()
                .id(studentId).username("alice").build());
        when(lessonScopeRepository.getReferenceById(scopeId))
            .thenReturn(com.github.k1mb1.vkr_backend.lesson.domain.LessonScope.builder()
                .id(scopeId).build());

        var savedAttendance = Attendance.builder()
            .student(com.github.k1mb1.vkr_backend.student.domain.Student.builder()
                .id(studentId).username("alice").build())
            .lessonScope(com.github.k1mb1.vkr_backend.lesson.domain.LessonScope.builder()
                .id(scopeId).build())
            .status(AttendanceStatus.PRESENT)
            .comment("good")
            .build();
        when(attendanceRepository.saveAll(anyList())).thenReturn(List.of(savedAttendance));

        var cellResponse = new AttendanceCellResponse(UUID.randomUUID(), studentId, scopeId, AttendanceStatus.PRESENT, "good");
        when(attendanceMapper.toCell(any(Attendance.class))).thenReturn(cellResponse);

        var result = service.upsertAll(request);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).studentId()).isEqualTo(studentId);
        assertThat(result.get(0).lessonScopeId()).isEqualTo(scopeId);
        assertThat(result.get(0).status()).isEqualTo(AttendanceStatus.PRESENT);
    }

    // -----------------------------------------------------------------------
    // upsertAll — existing record is updated (not re-inserted)
    // -----------------------------------------------------------------------

    @Test
    void upsertAll_existingRecord_updatesStatusAndComment() {
        var studentId = UUID.randomUUID();
        var scopeId = UUID.randomUUID();

        var student = com.github.k1mb1.vkr_backend.student.domain.Student.builder()
            .id(studentId).username("bob").build();
        var scope = com.github.k1mb1.vkr_backend.lesson.domain.LessonScope.builder()
            .id(scopeId).build();

        var existing = Attendance.builder()
            .student(student)
            .lessonScope(scope)
            .status(AttendanceStatus.ABSENT)
            .build();

        when(attendanceRepository.findByLessonScopeIdInAndStudentIdIn(anyList(), anyList()))
            .thenReturn(List.of(existing));
        when(attendanceRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));
        when(attendanceMapper.toCell(any(Attendance.class)))
            .thenReturn(new AttendanceCellResponse(UUID.randomUUID(), studentId, scopeId, AttendanceStatus.EXCUSED, "sick"));

        var item = new UpsertAttendanceRequest(studentId, scopeId, AttendanceStatus.EXCUSED, "sick");
        var result = service.upsertAll(new BulkUpsertAttendanceRequest(List.of(item)));

        assertThat(result).hasSize(1);
        // Verify the existing entity had status/comment updated
        assertThat(existing.getStatus()).isEqualTo(AttendanceStatus.EXCUSED);
        assertThat(existing.getComment()).isEqualTo("sick");
    }
}
