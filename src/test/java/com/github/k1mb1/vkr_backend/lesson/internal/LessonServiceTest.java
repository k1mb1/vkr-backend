package com.github.k1mb1.vkr_backend.lesson.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.k1mb1.vkr_backend.common.error.ResourceNotFoundException;
import com.github.k1mb1.vkr_backend.grading.GradingApi;
import com.github.k1mb1.vkr_backend.group.GroupReferenceService;
import com.github.k1mb1.vkr_backend.lesson.LessonScopesApi;
import com.github.k1mb1.vkr_backend.lesson.domain.Lesson;
import com.github.k1mb1.vkr_backend.lesson.domain.LessonScope;
import com.github.k1mb1.vkr_backend.lesson.domain.LessonType;
import com.github.k1mb1.vkr_backend.lesson.web.requests.BulkCreateLessonsRequest;
import com.github.k1mb1.vkr_backend.lesson.web.responses.LessonResponse;
import com.github.k1mb1.vkr_backend.subject.domain.Subject;
import com.github.k1mb1.vkr_backend.subject.internal.SubjectRepository;
import com.github.k1mb1.vkr_backend.subject.internal.TeacherSubjectPermissionRepository;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LessonServiceTest {

    @Mock
    LessonRepository lessonRepository;

    @Mock
    LessonMapper lessonMapper;

    @Mock
    SubjectRepository subjectRepository;

    @Mock
    TeacherSubjectPermissionRepository permissionRepository;

    @Mock
    LessonScopesApi lessonScopesApi;

    @Mock
    GradingApi gradingApi;

    @Mock
    GroupReferenceService groupReferenceService;

    @InjectMocks
    LessonService service;

    @Captor
    ArgumentCaptor<List<Lesson>> lessonsCaptor;

    // ---- schedule (pure) ----

    @Test
    void scheduleGeneratesWeeklyPatternUntilCount() {
        var monday = LocalDate.of(2026, 1, 5); // понедельник
        var dates = LessonService.schedule(
            monday, 5, List.of(List.of(DayOfWeek.MONDAY, DayOfWeek.THURSDAY))
        );

        assertThat(dates).containsExactly(
            LocalDate.of(2026, 1, 5),
            LocalDate.of(2026, 1, 8),
            LocalDate.of(2026, 1, 12),
            LocalDate.of(2026, 1, 15),
            LocalDate.of(2026, 1, 19)
        );
    }

    @Test
    void scheduleSkipsEmptyWeeksInCyclingPattern() {
        var monday = LocalDate.of(2026, 1, 5);
        var dates = LessonService.schedule(
            monday, 3, List.of(List.of(DayOfWeek.MONDAY, DayOfWeek.THURSDAY), List.of())
        );

        // через неделю: пн/чт первой недели, затем пропуск, затем пн третьей недели
        assertThat(dates).containsExactly(
            LocalDate.of(2026, 1, 5),
            LocalDate.of(2026, 1, 8),
            LocalDate.of(2026, 1, 19)
        );
    }

    @Test
    void scheduleSortsDaysWithinWeek() {
        var monday = LocalDate.of(2026, 1, 5);
        var dates = LessonService.schedule(
            monday, 2, List.of(List.of(DayOfWeek.THURSDAY, DayOfWeek.MONDAY))
        );

        assertThat(dates).containsExactly(
            LocalDate.of(2026, 1, 5),  // MON раньше THU несмотря на порядок в списке
            LocalDate.of(2026, 1, 8)
        );
    }

    // ---- earliestStartedAt (pure) ----

    @Test
    void earliestStartedAtReturnsMinIgnoringNulls() {
        var lesson = Lesson.builder()
            .id(UUID.randomUUID())
            .scopes(new HashSet<>(Set.of(
                LessonScope.builder().id(UUID.randomUUID()).startedAt(LocalDate.of(2026, 3, 10)).build(),
                LessonScope.builder().id(UUID.randomUUID()).startedAt(LocalDate.of(2026, 2, 1)).build(),
                LessonScope.builder().id(UUID.randomUUID()).startedAt(null).build()
            )))
            .build();

        assertThat(LessonService.earliestStartedAt(lesson)).isEqualTo(LocalDate.of(2026, 2, 1));
    }

    @Test
    void earliestStartedAtNullWhenNoDates() {
        var lesson = Lesson.builder().id(UUID.randomUUID()).scopes(new HashSet<>()).build();
        assertThat(LessonService.earliestStartedAt(lesson)).isNull();
    }

    // ---- bulkCreate ----

    @Test
    void bulkCreateContinuesOrderIndexAndAssignsDefaultTopics() {
        var subjectId = UUID.randomUUID();
        when(subjectRepository.findById(subjectId))
            .thenReturn(Optional.of(Subject.builder().id(subjectId).name("Math").build()));
        when(subjectRepository.getReferenceById(subjectId))
            .thenReturn(Subject.builder().id(subjectId).name("Math").build());
        when(lessonRepository.findMaxOrderIndex(subjectId, LessonType.LECTURE)).thenReturn(null);
        when(lessonRepository.findMaxOrderIndex(subjectId, LessonType.PRACTICE)).thenReturn(2);
        when(lessonRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(lessonMapper.toResponse(any(), any(), any()))
            .thenReturn(mock(LessonResponse.class));

        service.bulkCreate(new BulkCreateLessonsRequest(subjectId, 2, 1));

        verify(lessonRepository).saveAll(lessonsCaptor.capture());
        var lessons = lessonsCaptor.getValue();
        assertThat(lessons).hasSize(3);
        assertThat(lessons).filteredOn(l -> l.getType() == LessonType.LECTURE)
            .extracting(Lesson::getOrderIndex).containsExactly(1, 2);
        assertThat(lessons).filteredOn(l -> l.getType() == LessonType.PRACTICE)
            .extracting(Lesson::getOrderIndex).containsExactly(3);
        assertThat(lessons).extracting(Lesson::getTopic)
            .contains("Лекция 1", "Лекция 2", "Практика 3");
    }

    @Test
    void bulkCreateThrowsWhenSubjectMissing() {
        var subjectId = UUID.randomUUID();
        when(subjectRepository.findById(subjectId)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
            service.bulkCreate(new BulkCreateLessonsRequest(subjectId, 1, 0))
        ).isInstanceOf(ResourceNotFoundException.class);
    }

    // ---- deleteLesson ----

    @Test
    void deleteLessonArchivesAndShiftsOrderIndexDown() {
        var id = UUID.randomUUID();
        var subjectId = UUID.randomUUID();
        var lesson = Lesson.builder()
            .id(id)
            .subject(Subject.builder().id(subjectId).build())
            .type(LessonType.LECTURE)
            .orderIndex(4)
            .scopes(new HashSet<>())
            .build();
        when(lessonRepository.findById(id)).thenReturn(Optional.of(lesson));

        service.deleteLesson(id);

        assertThat(lesson.isArchived()).isTrue();
        verify(lessonRepository).save(lesson);
        verify(lessonRepository).shiftOrderIndexDown(eq(subjectId), eq(LessonType.LECTURE), eq(4));
    }
}
