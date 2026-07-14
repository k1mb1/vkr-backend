package com.github.k1mb1.vkr_backend.journal.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.github.k1mb1.vkr_backend.common.exception.ResourceNotFoundException;
import com.github.k1mb1.vkr_backend.lesson.domain.LessonEntity;
import com.github.k1mb1.vkr_backend.lesson.domain.LessonScopeEntity;
import com.github.k1mb1.vkr_backend.subject.domain.SubjectEntity;
import com.github.k1mb1.vkr_backend.teacher.domain.TeacherSubjectPermissionEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Пути разрешения занятий по фильтру (scope/lesson/все видимые) — default-метод
 * {@link JournalLessonRepository#resolveLessons}, общий для всех таблиц журнала
 * (бывшая логика LessonResolver).
 */
class JournalLessonRepositoryResolveTest {

    final JournalLessonRepository lessonRepository = mock(JournalLessonRepository.class, CALLS_REAL_METHODS);
    final JournalLessonScopeRepository scopeRepository = mock(JournalLessonScopeRepository.class);

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
    void resolveByScopeIdReturnsScopeLesson() {
        var subject = subject(subjectId);
        var lesson = lesson(UUID.randomUUID(), subject);
        var scopeId = UUID.randomUUID();
        var scope = LessonScopeEntity.builder().id(scopeId).lesson(lesson).build();
        doReturn(Optional.of(scope)).when(scopeRepository).findWithDetailsById(scopeId);

        var result = lessonRepository.resolveLessons(scopeRepository, permission(subject), scopeId, null);

        assertThat(result).containsExactly(lesson);
    }

    @Test
    void resolveByScopeIdThrowsWhenScopeMissing() {
        var scopeId = UUID.randomUUID();
        doReturn(Optional.empty()).when(scopeRepository).findWithDetailsById(scopeId);

        assertThatThrownBy(() ->
                        lessonRepository.resolveLessons(scopeRepository, permission(subject(subjectId)), scopeId, null))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void resolveByScopeIdRejectsScopeFromAnotherSubject() {
        var lesson = lesson(UUID.randomUUID(), subject(UUID.randomUUID()));
        var scopeId = UUID.randomUUID();
        var scope = LessonScopeEntity.builder().id(scopeId).lesson(lesson).build();
        doReturn(Optional.of(scope)).when(scopeRepository).findWithDetailsById(scopeId);

        assertThatThrownBy(() ->
                        lessonRepository.resolveLessons(scopeRepository, permission(subject(subjectId)), scopeId, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not belong to subject");
    }

    @Test
    void resolveByScopeIdRejectsMismatchedLessonId() {
        var subject = subject(subjectId);
        var lesson = lesson(UUID.randomUUID(), subject);
        var scopeId = UUID.randomUUID();
        var scope = LessonScopeEntity.builder().id(scopeId).lesson(lesson).build();
        doReturn(Optional.of(scope)).when(scopeRepository).findWithDetailsById(scopeId);

        assertThatThrownBy(() -> lessonRepository.resolveLessons(
                        scopeRepository, permission(subject), scopeId, UUID.randomUUID()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("belongs to lesson");
    }

    @Test
    void resolveByLessonIdReturnsLesson() {
        var subject = subject(subjectId);
        var lessonId = UUID.randomUUID();
        var lesson = lesson(lessonId, subject);
        doReturn(Optional.of(lesson)).when(lessonRepository).findById(lessonId);

        var result = lessonRepository.resolveLessons(scopeRepository, permission(subject), null, lessonId);

        assertThat(result).containsExactly(lesson);
    }

    @Test
    void resolveByLessonIdThrowsWhenLessonMissing() {
        var lessonId = UUID.randomUUID();
        doReturn(Optional.empty()).when(lessonRepository).findById(lessonId);

        assertThatThrownBy(() -> lessonRepository.resolveLessons(
                        scopeRepository, permission(subject(subjectId)), null, lessonId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void resolveWithoutFilterDelegatesToVisibilitySpecification() {
        var subject = subject(subjectId);
        var lessons = List.of(lesson(UUID.randomUUID(), subject));
        doReturn(lessons).when(lessonRepository).findAll(any(org.springframework.data.jpa.domain.Specification.class));

        var result = lessonRepository.resolveLessons(scopeRepository, permission(subject), null, null);

        assertThat(result).isEqualTo(lessons);
    }
}
