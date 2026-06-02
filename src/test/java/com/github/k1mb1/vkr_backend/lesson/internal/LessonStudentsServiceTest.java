package com.github.k1mb1.vkr_backend.lesson.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.github.k1mb1.vkr_backend.group.domain.Group;
import com.github.k1mb1.vkr_backend.group.domain.Subgroup;
import com.github.k1mb1.vkr_backend.lesson.domain.Lesson;
import com.github.k1mb1.vkr_backend.lesson.domain.LessonScope;
import com.github.k1mb1.vkr_backend.lesson.domain.LessonType;
import com.github.k1mb1.vkr_backend.student.domain.Student;
import com.github.k1mb1.vkr_backend.student.internal.StudentRepository;
import com.github.k1mb1.vkr_backend.subject.domain.Subject;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LessonStudentsServiceTest {

    @Mock StudentRepository studentRepository;

    @InjectMocks LessonStudentsService service;

    // -------------------------------------------------------------------------
    // studentsOf(Lesson) — allGroups scope → fetches all groups of the subject
    // -------------------------------------------------------------------------

    @Test
    void studentsOfLesson_allGroupsScope_returnsStudentsFromAllSubjectGroups() {
        var groupId1 = UUID.randomUUID();
        var groupId2 = UUID.randomUUID();
        var group1 = Group.builder().id(groupId1).name("Group A").build();
        var group2 = Group.builder().id(groupId2).name("Group B").build();

        var subject = Subject.builder()
            .id(UUID.randomUUID())
            .name("Math")
            .groups(Set.of(group1, group2))
            .build();

        var lesson = Lesson.builder()
            .id(UUID.randomUUID())
            .subject(subject)
            .type(LessonType.LECTURE)
            .orderIndex(1)
            .build();

        var scope = LessonScope.builder()
            .id(UUID.randomUUID())
            .lesson(lesson)
            .allGroups(true)
            .startedAt(LocalDate.of(2025, 1, 10))
            .build();
        lesson.getScopes().add(scope);

        var s1 = Student.builder().id(UUID.randomUUID()).username("bob").group(group1).build();
        var s2 = Student.builder().id(UUID.randomUUID()).username("alice").group(group2).build();

        when(studentRepository.findByGroupIdAndArchivedAtIsNull(groupId1)).thenReturn(List.of(s1));
        when(studentRepository.findByGroupIdAndArchivedAtIsNull(groupId2)).thenReturn(List.of(s2));

        var result = service.studentsOf(lesson);

        assertThat(result).containsExactly(s2, s1); // sorted by username: alice, bob
    }

    // -------------------------------------------------------------------------
    // studentsOf(Lesson) — dedup: same student appears in multiple scopes
    // -------------------------------------------------------------------------

    @Test
    void studentsOfLesson_sameStudentInTwoScopes_deduplicates() {
        var groupId = UUID.randomUUID();
        var group = Group.builder().id(groupId).name("Group C").build();

        var subgroupId1 = UUID.randomUUID();
        var subgroupId2 = UUID.randomUUID();
        var subgroup1 = Subgroup.builder().id(subgroupId1).index(1).group(group).build();
        var subgroup2 = Subgroup.builder().id(subgroupId2).index(2).group(group).build();

        var subject = Subject.builder()
            .id(UUID.randomUUID())
            .name("Physics")
            .groups(Set.of(group))
            .build();

        var lesson = Lesson.builder()
            .id(UUID.randomUUID())
            .subject(subject)
            .type(LessonType.PRACTICE)
            .orderIndex(1)
            .build();

        // scope1: specific subgroup1
        var scope1 = LessonScope.builder()
            .id(UUID.randomUUID())
            .lesson(lesson)
            .group(group)
            .allowedSubgroup(subgroup1)
            .allGroups(false)
            .startedAt(LocalDate.of(2025, 2, 1))
            .build();
        // scope2: specific subgroup2
        var scope2 = LessonScope.builder()
            .id(UUID.randomUUID())
            .lesson(lesson)
            .group(group)
            .allowedSubgroup(subgroup2)
            .allGroups(false)
            .startedAt(LocalDate.of(2025, 2, 8))
            .build();

        lesson.getScopes().add(scope1);
        lesson.getScopes().add(scope2);

        var sharedStudentId = UUID.randomUUID();
        var sharedStudent = Student.builder()
            .id(sharedStudentId)
            .username("charlie")
            .group(group)
            .subgroup(subgroup1)
            .build();
        var onlyInSub2 = Student.builder()
            .id(UUID.randomUUID())
            .username("diana")
            .group(group)
            .subgroup(subgroup2)
            .build();

        when(studentRepository.findByGroupIdAndSubgroupIdAndArchivedAtIsNull(groupId, subgroupId1))
            .thenReturn(List.of(sharedStudent));
        when(studentRepository.findByGroupIdAndSubgroupIdAndArchivedAtIsNull(groupId, subgroupId2))
            .thenReturn(List.of(sharedStudent, onlyInSub2));

        var result = service.studentsOf(lesson);

        // sharedStudent deduplicated, sorted by username
        assertThat(result).containsExactly(sharedStudent, onlyInSub2); // charlie, diana
        assertThat(result).hasSize(2);
    }

    // -------------------------------------------------------------------------
    // studentsOf(Lesson) — scope with group but no subgroup → whole group
    // -------------------------------------------------------------------------

    @Test
    void studentsOfLesson_groupScopeNoSubgroup_returnsWholeGroup() {
        var groupId = UUID.randomUUID();
        var group = Group.builder().id(groupId).name("Group D").build();

        var subject = Subject.builder()
            .id(UUID.randomUUID())
            .name("History")
            .groups(Set.of(group))
            .build();

        var lesson = Lesson.builder()
            .id(UUID.randomUUID())
            .subject(subject)
            .type(LessonType.LECTURE)
            .orderIndex(2)
            .build();

        var scope = LessonScope.builder()
            .id(UUID.randomUUID())
            .lesson(lesson)
            .group(group)
            .allowedSubgroup(null)
            .allGroups(false)
            .startedAt(LocalDate.of(2025, 3, 5))
            .build();
        lesson.getScopes().add(scope);

        var s1 = Student.builder().id(UUID.randomUUID()).username("zoe").group(group).build();
        var s2 = Student.builder().id(UUID.randomUUID()).username("anna").group(group).build();

        when(studentRepository.findByGroupIdAndArchivedAtIsNull(groupId)).thenReturn(List.of(s1, s2));

        var result = service.studentsOf(lesson);

        assertThat(result).containsExactly(s2, s1); // sorted: anna, zoe
    }

    // -------------------------------------------------------------------------
    // studentsOf(Lesson) — scope with null group (and not allGroups) → no students
    // -------------------------------------------------------------------------

    @Test
    void studentsOfLesson_scopeWithNullGroupAndNotAllGroups_returnsEmpty() {
        var subject = Subject.builder()
            .id(UUID.randomUUID())
            .name("PE")
            .groups(Set.of())
            .build();

        var lesson = Lesson.builder()
            .id(UUID.randomUUID())
            .subject(subject)
            .type(LessonType.PRACTICE)
            .orderIndex(1)
            .build();

        // scope with group=null and allGroups=false → collectScopeStudents returns early
        var scope = LessonScope.builder()
            .id(UUID.randomUUID())
            .lesson(lesson)
            .group(null)
            .allGroups(false)
            .startedAt(LocalDate.of(2025, 4, 1))
            .build();
        lesson.getScopes().add(scope);

        var result = service.studentsOf(lesson);

        assertThat(result).isEmpty();
    }

    // -------------------------------------------------------------------------
    // studentsOf(LessonScope) — allGroups scope
    // -------------------------------------------------------------------------

    @Test
    void studentsOfScope_allGroupsScope_returnsAllSubjectGroupStudents() {
        var groupId = UUID.randomUUID();
        var group = Group.builder().id(groupId).name("Group E").build();

        var subject = Subject.builder()
            .id(UUID.randomUUID())
            .name("Chemistry")
            .groups(Set.of(group))
            .build();

        var lesson = Lesson.builder()
            .id(UUID.randomUUID())
            .subject(subject)
            .type(LessonType.LECTURE)
            .orderIndex(1)
            .build();

        var scope = LessonScope.builder()
            .id(UUID.randomUUID())
            .lesson(lesson)
            .allGroups(true)
            .startedAt(LocalDate.of(2025, 5, 1))
            .build();

        var s1 = Student.builder().id(UUID.randomUUID()).username("mike").group(group).build();
        var s2 = Student.builder().id(UUID.randomUUID()).username("kate").group(group).build();

        when(studentRepository.findByGroupIdAndArchivedAtIsNull(groupId)).thenReturn(List.of(s1, s2));

        var result = service.studentsOf(scope);

        assertThat(result).containsExactly(s2, s1); // sorted: kate, mike
    }

    // -------------------------------------------------------------------------
    // studentsOf(LessonScope) — specific subgroup scope
    // -------------------------------------------------------------------------

    @Test
    void studentsOfScope_specificSubgroupScope_returnsOnlySubgroupStudents() {
        var groupId = UUID.randomUUID();
        var subgroupId = UUID.randomUUID();
        var group = Group.builder().id(groupId).name("Group F").build();
        var subgroup = Subgroup.builder().id(subgroupId).index(1).group(group).build();

        var subject = Subject.builder()
            .id(UUID.randomUUID())
            .name("Biology")
            .groups(Set.of(group))
            .build();

        var lesson = Lesson.builder()
            .id(UUID.randomUUID())
            .subject(subject)
            .type(LessonType.PRACTICE)
            .orderIndex(1)
            .build();

        var scope = LessonScope.builder()
            .id(UUID.randomUUID())
            .lesson(lesson)
            .group(group)
            .allowedSubgroup(subgroup)
            .allGroups(false)
            .startedAt(LocalDate.of(2025, 6, 1))
            .build();

        var s1 = Student.builder().id(UUID.randomUUID()).username("nina").group(group).subgroup(subgroup).build();

        when(studentRepository.findByGroupIdAndSubgroupIdAndArchivedAtIsNull(groupId, subgroupId))
            .thenReturn(List.of(s1));

        var result = service.studentsOf(scope);

        assertThat(result).containsExactly(s1);
    }

    // -------------------------------------------------------------------------
    // studentsOf(Lesson) — lesson with no scopes → empty
    // -------------------------------------------------------------------------

    @Test
    void studentsOfLesson_noScopes_returnsEmpty() {
        var subject = Subject.builder()
            .id(UUID.randomUUID())
            .name("Art")
            .groups(Set.of())
            .build();

        var lesson = Lesson.builder()
            .id(UUID.randomUUID())
            .subject(subject)
            .type(LessonType.LECTURE)
            .orderIndex(1)
            .build();

        var result = service.studentsOf(lesson);

        assertThat(result).isEmpty();
    }

    // -------------------------------------------------------------------------
    // studentsOf(Lesson) — sorted by username
    // -------------------------------------------------------------------------

    @Test
    void studentsOfLesson_resultIsSortedByUsername() {
        var groupId = UUID.randomUUID();
        var group = Group.builder().id(groupId).name("Group G").build();

        var subject = Subject.builder()
            .id(UUID.randomUUID())
            .name("Geography")
            .groups(Set.of(group))
            .build();

        var lesson = Lesson.builder()
            .id(UUID.randomUUID())
            .subject(subject)
            .type(LessonType.LECTURE)
            .orderIndex(1)
            .build();

        var scope = LessonScope.builder()
            .id(UUID.randomUUID())
            .lesson(lesson)
            .allGroups(true)
            .startedAt(LocalDate.of(2025, 7, 1))
            .build();
        lesson.getScopes().add(scope);

        var sZack  = Student.builder().id(UUID.randomUUID()).username("zack").group(group).build();
        var sAlice = Student.builder().id(UUID.randomUUID()).username("alice").group(group).build();
        var sMike  = Student.builder().id(UUID.randomUUID()).username("mike").group(group).build();

        when(studentRepository.findByGroupIdAndArchivedAtIsNull(groupId))
            .thenReturn(List.of(sZack, sAlice, sMike));

        var result = service.studentsOf(lesson);

        assertThat(result)
            .extracting(Student::getUsername)
            .containsExactly("alice", "mike", "zack");
    }
}
