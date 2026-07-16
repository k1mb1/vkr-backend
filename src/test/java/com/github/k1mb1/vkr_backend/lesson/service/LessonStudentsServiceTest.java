package com.github.k1mb1.vkr_backend.lesson.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.k1mb1.vkr_backend.group.domain.GroupEntity;
import com.github.k1mb1.vkr_backend.group.domain.StudentEntity;
import com.github.k1mb1.vkr_backend.group.domain.SubgroupEntity;
import com.github.k1mb1.vkr_backend.lesson.domain.LessonEntity;
import com.github.k1mb1.vkr_backend.lesson.domain.LessonScopeEntity;
import com.github.k1mb1.vkr_backend.lesson.repository.LessonStudentRepository;
import com.github.k1mb1.vkr_backend.subject.domain.SubjectEntity;
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
    LessonStudentRepository lessonStudentRepository;

    @InjectMocks
    LessonStudentsService service;

    private GroupEntity group(String name) {
        return GroupEntity.builder().id(UUID.randomUUID()).name(name).build();
    }

    private StudentEntity student(String username, GroupEntity group, SubgroupEntity subgroup) {
        return StudentEntity.builder()
                .id(UUID.randomUUID())
                .username(username)
                .group(group)
                .subgroup(subgroup)
                .build();
    }

    @Test
    void emptyScopesReturnEmptyWithoutQuery() {
        assertThat(service.studentsOf(List.of())).isEmpty();
        verify(lessonStudentRepository, never()).findByGroupIdInAndArchivedAtIsNull(any());
    }

    @Test
    void groupScopeReturnsGroupRosterSortedByUsername() {
        var group = group("G1");
        var lesson = LessonEntity.builder().id(UUID.randomUUID()).build();
        var scope = LessonScopeEntity.builder()
                .id(UUID.randomUUID())
                .lesson(lesson)
                .group(group)
                .allGroups(false)
                .build();
        var sveta = student("Света", group, null);
        var anna = student("Анна", group, null);
        when(lessonStudentRepository.findByGroupIdInAndArchivedAtIsNull(any())).thenReturn(List.of(sveta, anna));

        var result = service.studentsOf(List.of(scope));

        assertThat(result).extracting(StudentEntity::getUsername).containsExactly("Анна", "Света");
    }

    @Test
    void subgroupRestrictedScopeFiltersBySubgroupInMemory() {
        var group = group("G1");
        var sub1 = SubgroupEntity.builder()
                .id(UUID.randomUUID())
                .index(1)
                .group(group)
                .build();
        var sub2 = SubgroupEntity.builder()
                .id(UUID.randomUUID())
                .index(2)
                .group(group)
                .build();
        var lesson = LessonEntity.builder().id(UUID.randomUUID()).build();
        var scope = LessonScopeEntity.builder()
                .id(UUID.randomUUID())
                .lesson(lesson)
                .group(group)
                .allowedSubgroup(sub1)
                .allGroups(false)
                .build();
        var inSub1 = student("Анна", group, sub1);
        var inSub2 = student("Борис", group, sub2);
        var noSub = student("Виктор", group, null);
        when(lessonStudentRepository.findByGroupIdInAndArchivedAtIsNull(any()))
                .thenReturn(List.of(inSub1, inSub2, noSub));

        var result = service.studentsOf(List.of(scope));

        assertThat(result).containsExactly(inSub1);
    }

    @Test
    void allGroupsScopeUnionsAllSubjectGroups() {
        var g1 = group("G1");
        var g2 = group("G2");
        var subject = SubjectEntity.builder()
                .id(UUID.randomUUID())
                .name("Math")
                .groups(Set.of(g1, g2))
                .build();
        var lesson =
                LessonEntity.builder().id(UUID.randomUUID()).subject(subject).build();
        var scope = LessonScopeEntity.builder()
                .id(UUID.randomUUID())
                .lesson(lesson)
                .allGroups(true)
                .build();
        var a = student("Анна", g1, null);
        var b = student("Борис", g2, null);
        when(lessonStudentRepository.findByGroupIdInAndArchivedAtIsNull(any())).thenReturn(List.of(a, b));

        var result = service.studentsOf(List.of(scope));

        assertThat(result).extracting(StudentEntity::getUsername).containsExactly("Анна", "Борис");
    }

    @Test
    void dedupesStudentSharedAcrossScopesAndBatchesQuery() {
        var group = group("G1");
        var lesson = LessonEntity.builder().id(UUID.randomUUID()).build();
        var scope1 = LessonScopeEntity.builder()
                .id(UUID.randomUUID())
                .lesson(lesson)
                .group(group)
                .allGroups(false)
                .build();
        var scope2 = LessonScopeEntity.builder()
                .id(UUID.randomUUID())
                .lesson(lesson)
                .group(group)
                .allGroups(false)
                .build();
        var anna = student("Анна", group, null);
        when(lessonStudentRepository.findByGroupIdInAndArchivedAtIsNull(any())).thenReturn(List.of(anna));

        var result = service.studentsOf(List.of(scope1, scope2));

        assertThat(result).containsExactly(anna);
        // батч: один запрос на все scope'ы, без N+1
        verify(lessonStudentRepository, times(1)).findByGroupIdInAndArchivedAtIsNull(any());
    }

    @Test
    void scopeWithNullGroupAndNotAllGroupsContributesNothing() {
        var lesson = LessonEntity.builder().id(UUID.randomUUID()).build();
        var scope = LessonScopeEntity.builder()
                .id(UUID.randomUUID())
                .lesson(lesson)
                .group(null)
                .allGroups(false)
                .build();

        var result = service.studentsOf(List.of(scope));

        assertThat(result).isEmpty();
        verify(lessonStudentRepository, never()).findByGroupIdInAndArchivedAtIsNull(any());
    }
}
