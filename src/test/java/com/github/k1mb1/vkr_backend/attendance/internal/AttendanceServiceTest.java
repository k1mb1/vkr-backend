package com.github.k1mb1.vkr_backend.attendance.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.github.k1mb1.vkr_backend.attendance.domain.Attendance;
import com.github.k1mb1.vkr_backend.attendance.domain.AttendanceStatus;
import com.github.k1mb1.vkr_backend.student.domain.Student;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock
    AttendanceRepository attendanceRepository;

    @Mock
    AttendanceMapper attendanceMapper;

    @InjectMocks
    AttendanceService service;

    @Test
    void summarizeReturnsEmptyMapWhenNoScopes() {
        var result = service.summarize(List.of(), List.of(UUID.randomUUID()));

        assertThat(result).isEmpty();
        verifyNoInteractions(attendanceRepository);
    }

    @Test
    void summarizeReturnsEmptyMapWhenNoStudents() {
        var result = service.summarize(List.of(UUID.randomUUID()), List.of());

        assertThat(result).isEmpty();
        verifyNoInteractions(attendanceRepository);
    }

    @Test
    void summarizeCountsStatusesPerStudent() {
        var scopeIds = List.of(UUID.randomUUID());
        var alice = student();
        var bob = student();
        var studentIds = List.of(alice.getId(), bob.getId());

        when(attendanceRepository.findByLessonScopeIdInAndStudentIdIn(scopeIds, studentIds))
            .thenReturn(List.of(
                attendance(alice, AttendanceStatus.PRESENT),
                attendance(alice, AttendanceStatus.PRESENT),
                attendance(alice, AttendanceStatus.LATE),
                attendance(alice, AttendanceStatus.ABSENT),
                attendance(bob, AttendanceStatus.EXCUSED)
            ));

        var result = service.summarize(scopeIds, studentIds);

        assertThat(result).hasSize(2);

        var aliceSummary = result.get(alice.getId());
        assertThat(aliceSummary.present()).isEqualTo(2);
        assertThat(aliceSummary.late()).isEqualTo(1);
        assertThat(aliceSummary.absent()).isEqualTo(1);
        assertThat(aliceSummary.excused()).isEqualTo(0);

        var bobSummary = result.get(bob.getId());
        assertThat(bobSummary.present()).isEqualTo(0);
        assertThat(bobSummary.excused()).isEqualTo(1);
    }

    private static Student student() {
        return Student.builder().id(UUID.randomUUID()).username("s").build();
    }

    private static Attendance attendance(Student student, AttendanceStatus status) {
        return Attendance.builder().student(student).status(status).build();
    }
}
