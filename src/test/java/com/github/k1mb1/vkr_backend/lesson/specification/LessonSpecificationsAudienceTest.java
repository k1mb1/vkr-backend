package com.github.k1mb1.vkr_backend.lesson.specification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.k1mb1.vkr_backend.group.domain.GroupEntity;
import com.github.k1mb1.vkr_backend.group.domain.SubgroupEntity;
import com.github.k1mb1.vkr_backend.lesson.domain.LessonEntity;
import com.github.k1mb1.vkr_backend.subject.domain.SubjectEntity;
import com.github.k1mb1.vkr_backend.teacher.domain.PermissionScopeEntity;
import com.github.k1mb1.vkr_backend.teacher.domain.TeacherSubjectPermissionEntity;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Статические проверки принадлежности и вычисление аудитории разрешения — бывшая
 * логика LessonResolver, теперь в {@link LessonSpecifications}; пути разрешения
 * занятий по фильтру проверяются в тестах сервисов attendance/grading.
 */
class LessonSpecificationsAudienceTest {

    final UUID subjectId = UUID.randomUUID();

    private SubjectEntity subject(UUID id) {
        return SubjectEntity.builder().id(id).name("Math").build();
    }

    private LessonEntity lesson(UUID id, SubjectEntity subject) {
        return LessonEntity.builder().id(id).subject(subject).build();
    }

    private TeacherSubjectPermissionEntity permission(SubjectEntity subject) {
        return TeacherSubjectPermissionEntity.builder()
                .id(UUID.randomUUID())
                .subject(subject)
                .build();
    }

    @Test
    void assertSameSubjectRejectsLessonOfAnotherSubject() {
        var lesson = lesson(UUID.randomUUID(), subject(UUID.randomUUID()));

        assertThatThrownBy(() -> LessonSpecifications.assertSameSubject(lesson, permission(subject(subjectId))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not belong to subject");
    }

    @Test
    void assertSameSubjectAcceptsLessonOfPermissionSubject() {
        var subject = subject(subjectId);
        LessonSpecifications.assertSameSubject(lesson(UUID.randomUUID(), subject), permission(subject));
    }

    @Test
    void assertLessonMatchRejectsMismatchedLessonId() {
        var lesson = lesson(UUID.randomUUID(), subject(subjectId));

        assertThatThrownBy(() -> LessonSpecifications.assertLessonMatch(lesson, UUID.randomUUID()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("belongs to lesson");
    }

    @Test
    void assertLessonMatchAcceptsNullOrSameLessonId() {
        var lesson = lesson(UUID.randomUUID(), subject(subjectId));
        LessonSpecifications.assertLessonMatch(lesson, null);
        LessonSpecifications.assertLessonMatch(lesson, lesson.getId());
    }

    @Test
    void audienceScopesEmptyWhenAllPermissions() {
        var permission = TeacherSubjectPermissionEntity.builder()
                .id(UUID.randomUUID())
                .subject(subject(subjectId))
                .allPermissions(true)
                .build();

        assertThat(LessonSpecifications.audienceScopes(permission)).isEmpty();
    }

    @Test
    void audienceScopesEmptyWhenAnyScopeHasNoGroup() {
        var permission = permission(subject(subjectId));
        permission.setScopes(Set.of(PermissionScopeEntity.builder()
                .id(UUID.randomUUID())
                .group(null)
                .build()));

        assertThat(LessonSpecifications.audienceScopes(permission)).isEmpty();
    }

    @Test
    void audienceScopesSortedByGroupNameThenSubgroupIndex() {
        var permission = permission(subject(subjectId));
        var groupB = GroupEntity.builder().id(UUID.randomUUID()).name("B").build();
        var groupA = GroupEntity.builder().id(UUID.randomUUID()).name("A").build();
        var sub2 = SubgroupEntity.builder()
                .id(UUID.randomUUID())
                .index(2)
                .group(groupA)
                .build();
        var sub1 = SubgroupEntity.builder()
                .id(UUID.randomUUID())
                .index(1)
                .group(groupA)
                .build();

        var scopeB = PermissionScopeEntity.builder()
                .id(UUID.randomUUID())
                .group(groupB)
                .build();
        var scopeA2 = PermissionScopeEntity.builder()
                .id(UUID.randomUUID())
                .group(groupA)
                .allowedSubgroup(sub2)
                .build();
        var scopeA1 = PermissionScopeEntity.builder()
                .id(UUID.randomUUID())
                .group(groupA)
                .allowedSubgroup(sub1)
                .build();
        permission.setScopes(Set.of(scopeB, scopeA2, scopeA1));

        var result = LessonSpecifications.audienceScopes(permission);

        // A (sub1) -> A (sub2) -> B
        assertThat(result).containsExactly(scopeA1, scopeA2, scopeB);
    }
}
