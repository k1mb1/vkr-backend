package com.github.k1mb1.vkr_backend.lesson.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.github.k1mb1.vkr_backend.common.error.ResourceNotFoundException;
import com.github.k1mb1.vkr_backend.group.domain.Group;
import com.github.k1mb1.vkr_backend.group.domain.Subgroup;
import com.github.k1mb1.vkr_backend.lesson.domain.Lesson;
import com.github.k1mb1.vkr_backend.lesson.domain.LessonScope;
import com.github.k1mb1.vkr_backend.subject.domain.PermissionScope;
import com.github.k1mb1.vkr_backend.subject.domain.Subject;
import com.github.k1mb1.vkr_backend.subject.domain.TeacherSubjectPermission;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LessonResolverTest {

    @Mock
    LessonRepository lessonRepository;

    @Mock
    LessonScopeRepository lessonScopeRepository;

    @InjectMocks
    LessonResolver resolver;

    final UUID subjectId = UUID.randomUUID();

    private Subject subject(UUID id) {
        return Subject.builder().id(id).name("Math").build();
    }

    private Lesson lesson(UUID id, Subject subject) {
        return Lesson.builder().id(id).subject(subject).build();
    }

    private TeacherSubjectPermission permission(Subject subject) {
        return TeacherSubjectPermission.builder()
            .id(UUID.randomUUID())
            .subject(subject)
            .build();
    }

    // ---- resolveLessons ----

    @Test
    void resolveByScopeIdReturnsScopeLesson() {
        var subject = subject(subjectId);
        var lesson = lesson(UUID.randomUUID(), subject);
        var scopeId = UUID.randomUUID();
        var scope = LessonScope.builder().id(scopeId).lesson(lesson).build();
        when(lessonScopeRepository.findById(scopeId)).thenReturn(Optional.of(scope));

        var result = resolver.resolveLessons(permission(subject), scopeId, null);

        assertThat(result).containsExactly(lesson);
    }

    @Test
    void resolveByScopeIdThrowsWhenScopeMissing() {
        var scopeId = UUID.randomUUID();
        when(lessonScopeRepository.findById(scopeId)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
            resolver.resolveLessons(permission(subject(subjectId)), scopeId, null)
        ).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void resolveByScopeIdRejectsScopeFromAnotherSubject() {
        var lesson = lesson(UUID.randomUUID(), subject(UUID.randomUUID()));
        var scopeId = UUID.randomUUID();
        var scope = LessonScope.builder().id(scopeId).lesson(lesson).build();
        when(lessonScopeRepository.findById(scopeId)).thenReturn(Optional.of(scope));

        assertThatThrownBy(() ->
            resolver.resolveLessons(permission(subject(subjectId)), scopeId, null)
        )
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("does not belong to subject");
    }

    @Test
    void resolveByScopeIdRejectsMismatchedLessonId() {
        var subject = subject(subjectId);
        var lesson = lesson(UUID.randomUUID(), subject);
        var scopeId = UUID.randomUUID();
        var scope = LessonScope.builder().id(scopeId).lesson(lesson).build();
        when(lessonScopeRepository.findById(scopeId)).thenReturn(Optional.of(scope));

        assertThatThrownBy(() ->
            resolver.resolveLessons(permission(subject), scopeId, UUID.randomUUID())
        )
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("belongs to lesson");
    }

    @Test
    void resolveByLessonIdReturnsLesson() {
        var subject = subject(subjectId);
        var lessonId = UUID.randomUUID();
        var lesson = lesson(lessonId, subject);
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));

        var result = resolver.resolveLessons(permission(subject), null, lessonId);

        assertThat(result).containsExactly(lesson);
    }

    @Test
    void resolveByLessonIdThrowsWhenLessonMissing() {
        var lessonId = UUID.randomUUID();
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
            resolver.resolveLessons(permission(subject(subjectId)), null, lessonId)
        ).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void resolveWithoutFilterDelegatesToSpecification() {
        var subject = subject(subjectId);
        var lessons = List.of(lesson(UUID.randomUUID(), subject));
        when(lessonRepository.findAllWithDetails(any())).thenReturn(lessons);

        var result = resolver.resolveLessons(permission(subject), null, null);

        assertThat(result).isEqualTo(lessons);
    }

    // ---- audienceScopes ----

    @Test
    void audienceScopesEmptyWhenAllPermissions() {
        var permission = TeacherSubjectPermission.builder()
            .id(UUID.randomUUID())
            .subject(subject(subjectId))
            .allPermissions(true)
            .build();

        assertThat(resolver.audienceScopes(permission)).isEmpty();
    }

    @Test
    void audienceScopesEmptyWhenAnyScopeHasNoGroup() {
        var permission = permission(subject(subjectId));
        permission.setScopes(Set.of(
            PermissionScope.builder().id(UUID.randomUUID()).group(null).build()
        ));

        assertThat(resolver.audienceScopes(permission)).isEmpty();
    }

    @Test
    void audienceScopesSortedByGroupNameThenSubgroupIndex() {
        var permission = permission(subject(subjectId));
        var groupB = Group.builder().id(UUID.randomUUID()).name("B").build();
        var groupA = Group.builder().id(UUID.randomUUID()).name("A").build();
        var sub2 = Subgroup.builder().id(UUID.randomUUID()).index(2).group(groupA).build();
        var sub1 = Subgroup.builder().id(UUID.randomUUID()).index(1).group(groupA).build();

        var scopeB = PermissionScope.builder().id(UUID.randomUUID()).group(groupB).build();
        var scopeA2 = PermissionScope.builder().id(UUID.randomUUID()).group(groupA).allowedSubgroup(sub2).build();
        var scopeA1 = PermissionScope.builder().id(UUID.randomUUID()).group(groupA).allowedSubgroup(sub1).build();
        permission.setScopes(Set.of(scopeB, scopeA2, scopeA1));

        var result = resolver.audienceScopes(permission);

        // A (sub1) -> A (sub2) -> B
        assertThat(result).containsExactly(scopeA1, scopeA2, scopeB);
    }
}
