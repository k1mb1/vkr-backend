package com.github.k1mb1.vkr_backend.lesson.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.k1mb1.vkr_backend.group.domain.Group;
import com.github.k1mb1.vkr_backend.group.domain.Subgroup;
import com.github.k1mb1.vkr_backend.lesson.domain.Lesson;
import com.github.k1mb1.vkr_backend.lesson.domain.LessonScope;
import com.github.k1mb1.vkr_backend.student.domain.Student;
import com.github.k1mb1.vkr_backend.student.internal.StudentRepository;
import com.github.k1mb1.vkr_backend.subject.domain.Subject;
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

    @Mock
    StudentRepository studentRepository;

    @InjectMocks
    LessonStudentsService service;

    private Group group(String name) {
        return Group.builder().id(UUID.randomUUID()).name(name).build();
    }

    private Student student(String username, Group group, Subgroup subgroup) {
        return Student.builder()
                .id(UUID.randomUUID())
                .username(username)
                .group(group)
                .subgroup(subgroup)
                .build();
    }

    @Test
    void emptyScopesReturnEmptyWithoutQuery() {
        assertThat(service.studentsOf(List.of())).isEmpty();
        verify(studentRepository, never()).findByGroupIdInAndArchivedAtIsNull(any());
    }

    @Test
    void groupScopeReturnsGroupRosterSortedByUsername() {
        var group = group("G1");
        var lesson = Lesson.builder().id(UUID.randomUUID()).build();
        var scope = LessonScope.builder()
                .id(UUID.randomUUID())
                .lesson(lesson)
                .group(group)
                .allGroups(false)
                .build();
        var sveta = student("Света", group, null);
        var anna = student("Анна", group, null);
        when(studentRepository.findByGroupIdInAndArchivedAtIsNull(any())).thenReturn(List.of(sveta, anna));

        var result = service.studentsOf(List.of(scope));

        assertThat(result).extracting(Student::getUsername).containsExactly("Анна", "Света");
    }

    @Test
    void subgroupRestrictedScopeFiltersBySubgroupInMemory() {
        var group = group("G1");
        var sub1 =
                Subgroup.builder().id(UUID.randomUUID()).index(1).group(group).build();
        var sub2 =
                Subgroup.builder().id(UUID.randomUUID()).index(2).group(group).build();
        var lesson = Lesson.builder().id(UUID.randomUUID()).build();
        var scope = LessonScope.builder()
                .id(UUID.randomUUID())
                .lesson(lesson)
                .group(group)
                .allowedSubgroup(sub1)
                .allGroups(false)
                .build();
        var inSub1 = student("Анна", group, sub1);
        var inSub2 = student("Борис", group, sub2);
        var noSub = student("Виктор", group, null);
        when(studentRepository.findByGroupIdInAndArchivedAtIsNull(any())).thenReturn(List.of(inSub1, inSub2, noSub));

        var result = service.studentsOf(List.of(scope));

        assertThat(result).containsExactly(inSub1);
    }

    @Test
    void allGroupsScopeUnionsAllSubjectGroups() {
        var g1 = group("G1");
        var g2 = group("G2");
        var subject = Subject.builder()
                .id(UUID.randomUUID())
                .name("Math")
                .groups(Set.of(g1, g2))
                .build();
        var lesson = Lesson.builder().id(UUID.randomUUID()).subject(subject).build();
        var scope = LessonScope.builder()
                .id(UUID.randomUUID())
                .lesson(lesson)
                .allGroups(true)
                .build();
        var a = student("Анна", g1, null);
        var b = student("Борис", g2, null);
        when(studentRepository.findByGroupIdInAndArchivedAtIsNull(any())).thenReturn(List.of(a, b));

        var result = service.studentsOf(List.of(scope));

        assertThat(result).extracting(Student::getUsername).containsExactly("Анна", "Борис");
    }

    @Test
    void dedupesStudentSharedAcrossScopesAndBatchesQuery() {
        var group = group("G1");
        var lesson = Lesson.builder().id(UUID.randomUUID()).build();
        var scope1 = LessonScope.builder()
                .id(UUID.randomUUID())
                .lesson(lesson)
                .group(group)
                .allGroups(false)
                .build();
        var scope2 = LessonScope.builder()
                .id(UUID.randomUUID())
                .lesson(lesson)
                .group(group)
                .allGroups(false)
                .build();
        var anna = student("Анна", group, null);
        when(studentRepository.findByGroupIdInAndArchivedAtIsNull(any())).thenReturn(List.of(anna));

        var result = service.studentsOf(List.of(scope1, scope2));

        assertThat(result).containsExactly(anna);
        // батч: один запрос на все scope'ы, без N+1
        verify(studentRepository, times(1)).findByGroupIdInAndArchivedAtIsNull(any());
    }

    @Test
    void scopeWithNullGroupAndNotAllGroupsContributesNothing() {
        var lesson = Lesson.builder().id(UUID.randomUUID()).build();
        var scope = LessonScope.builder()
                .id(UUID.randomUUID())
                .lesson(lesson)
                .group(null)
                .allGroups(false)
                .build();

        var result = service.studentsOf(List.of(scope));

        assertThat(result).isEmpty();
        verify(studentRepository, never()).findByGroupIdInAndArchivedAtIsNull(any());
    }
}
