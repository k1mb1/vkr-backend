package com.github.k1mb1.vkr_backend.attendance.internal;

import com.github.k1mb1.vkr_backend.attendance.domain.Attendance;
import com.github.k1mb1.vkr_backend.attendance.domain.AttendanceStatus;
import com.github.k1mb1.vkr_backend.group.domain.Group;
import com.github.k1mb1.vkr_backend.group.domain.Subgroup;
import com.github.k1mb1.vkr_backend.lesson.domain.Lesson;
import com.github.k1mb1.vkr_backend.lesson.domain.LessonScope;
import com.github.k1mb1.vkr_backend.lesson.domain.LessonType;
import com.github.k1mb1.vkr_backend.student.domain.Student;
import com.github.k1mb1.vkr_backend.subject.domain.Subject;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AttendanceMapperTest {

    private final AttendanceMapper mapper = new AttendanceMapperImpl();

    // -----------------------------------------------------------------------
    // toCell
    // -----------------------------------------------------------------------

    @Test
    void toCell_mapsAllFields() {
        var studentId = UUID.randomUUID();
        var scopeId = UUID.randomUUID();
        var attendanceId = UUID.randomUUID();

        var student = Student.builder().id(studentId).username("Alice").build();
        var scope = LessonScope.builder().id(scopeId).build();

        var attendance = Attendance.builder()
            .id(attendanceId)
            .student(student)
            .lessonScope(scope)
            .status(AttendanceStatus.PRESENT)
            .comment("good")
            .build();

        var result = mapper.toCell(attendance);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(attendanceId);
        assertThat(result.studentId()).isEqualTo(studentId);
        assertThat(result.lessonScopeId()).isEqualTo(scopeId);
        assertThat(result.status()).isEqualTo(AttendanceStatus.PRESENT);
        assertThat(result.comment()).isEqualTo("good");
    }

    @Test
    void toCell_nullReturnsNull() {
        assertThat(mapper.toCell(null)).isNull();
    }

    // -----------------------------------------------------------------------
    // toTableStudent — with subgroup
    // -----------------------------------------------------------------------

    @Test
    void toTableStudent_withSubgroup_mapsAllFields() {
        var groupId = UUID.randomUUID();
        var subgroupId = UUID.randomUUID();
        var studentId = UUID.randomUUID();

        var group = Group.builder().id(groupId).name("Group A").build();
        var subgroup = Subgroup.builder().id(subgroupId).index(2).group(group).build();
        var student = Student.builder()
            .id(studentId)
            .username("Bob")
            .group(group)
            .subgroup(subgroup)
            .build();

        var result = mapper.toTableStudent(student);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(studentId);
        assertThat(result.username()).isEqualTo("Bob");
        assertThat(result.groupId()).isEqualTo(groupId);
        assertThat(result.groupName()).isEqualTo("Group A");
        assertThat(result.subgroupId()).isEqualTo(subgroupId);
        assertThat(result.subgroupIndex()).isEqualTo(2);
    }

    @Test
    void toTableStudent_withoutSubgroup_subgroupFieldsAreNull() {
        var groupId = UUID.randomUUID();
        var group = Group.builder().id(groupId).name("Group B").build();
        var student = Student.builder()
            .id(UUID.randomUUID())
            .username("Carol")
            .group(group)
            .subgroup(null)
            .build();

        var result = mapper.toTableStudent(student);

        assertThat(result.subgroupId()).isNull();
        assertThat(result.subgroupIndex()).isNull();
    }

    @Test
    void toTableStudent_nullReturnsNull() {
        assertThat(mapper.toTableStudent(null)).isNull();
    }

    // -----------------------------------------------------------------------
    // toTableLesson — with group + allowedSubgroup
    // -----------------------------------------------------------------------

    @Test
    void toTableLesson_withGroupAndSubgroup_mapsAllFields() {
        var scopeId = UUID.randomUUID();
        var lessonId = UUID.randomUUID();
        var groupId = UUID.randomUUID();
        var subgroupId = UUID.randomUUID();

        var subject = Subject.builder().id(UUID.randomUUID()).name("Math").build();
        var group = Group.builder().id(groupId).name("Group C").build();
        var subgroup = Subgroup.builder().id(subgroupId).index(1).group(group).build();

        var lesson = Lesson.builder()
            .id(lessonId)
            .subject(subject)
            .type(LessonType.LECTURE)
            .orderIndex(3)
            .topic("Topic X")
            .active(true)
            .build();

        var scope = LessonScope.builder()
            .id(scopeId)
            .lesson(lesson)
            .group(group)
            .allowedSubgroup(subgroup)
            .startedAt(LocalDate.of(2025, 1, 15))
            .allGroups(false)
            .build();

        var result = mapper.toTableLesson(scope);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(scopeId);
        assertThat(result.lessonId()).isEqualTo(lessonId);
        assertThat(result.startedAt()).isEqualTo(LocalDate.of(2025, 1, 15));
        assertThat(result.type()).isEqualTo(LessonType.LECTURE);
        assertThat(result.orderIndex()).isEqualTo(3);
        assertThat(result.topic()).isEqualTo("Topic X");
        assertThat(result.active()).isTrue();
        assertThat(result.groupId()).isEqualTo(groupId);
        assertThat(result.groupName()).isEqualTo("Group C");
        assertThat(result.allowedSubgroupId()).isEqualTo(subgroupId);
        assertThat(result.allowedSubgroupIndex()).isEqualTo(1);
        assertThat(result.allGroups()).isFalse();
    }

    @Test
    void toTableLesson_allGroups_groupAndSubgroupFieldsAreNull() {
        var subject = Subject.builder().id(UUID.randomUUID()).name("Math").build();
        var lesson = Lesson.builder()
            .id(UUID.randomUUID())
            .subject(subject)
            .type(LessonType.PRACTICE)
            .orderIndex(1)
            .active(false)
            .build();

        var scope = LessonScope.builder()
            .id(UUID.randomUUID())
            .lesson(lesson)
            .group(null)
            .allowedSubgroup(null)
            .startedAt(LocalDate.of(2025, 3, 10))
            .allGroups(true)
            .build();

        var result = mapper.toTableLesson(scope);

        assertThat(result.allGroups()).isTrue();
        assertThat(result.groupId()).isNull();
        assertThat(result.groupName()).isNull();
        assertThat(result.allowedSubgroupId()).isNull();
        assertThat(result.allowedSubgroupIndex()).isNull();
    }

    @Test
    void toTableLesson_nullReturnsNull() {
        assertThat(mapper.toTableLesson(null)).isNull();
    }
}
