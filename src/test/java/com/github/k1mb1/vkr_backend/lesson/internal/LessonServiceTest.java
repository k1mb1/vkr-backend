package com.github.k1mb1.vkr_backend.lesson.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.k1mb1.vkr_backend.common.error.ResourceNotFoundException;
import com.github.k1mb1.vkr_backend.grading.GradingApi;
import com.github.k1mb1.vkr_backend.lesson.LessonScopesApi;
import com.github.k1mb1.vkr_backend.lesson.domain.Lesson;
import com.github.k1mb1.vkr_backend.lesson.domain.LessonScope;
import com.github.k1mb1.vkr_backend.lesson.domain.LessonType;
import com.github.k1mb1.vkr_backend.lesson.web.requests.BulkCreateLessonsRequest;
import com.github.k1mb1.vkr_backend.lesson.web.requests.UpdateLessonRequest;
import com.github.k1mb1.vkr_backend.lesson.web.responses.LessonResponse;
import com.github.k1mb1.vkr_backend.subject.domain.Subject;
import com.github.k1mb1.vkr_backend.subject.internal.SubjectRepository;
import com.github.k1mb1.vkr_backend.subject.internal.TeacherSubjectPermissionRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LessonServiceTest {

    @Mock LessonRepository lessonRepository;
    @Mock LessonMapper lessonMapper;
    @Mock SubjectRepository subjectRepository;
    @Mock TeacherSubjectPermissionRepository permissionRepository;
    @Mock LessonScopesApi lessonScopesApi;
    @Mock GradingApi gradingApi;

    @InjectMocks LessonService service;

    // -------------------------------------------------------------------------
    // getLessonById
    // -------------------------------------------------------------------------

    @Test
    void getLessonById_notFound_throwsResourceNotFoundException() {
        var id = UUID.randomUUID();
        when(lessonRepository.findWithDetailsById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getLessonById(id))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Lesson");
    }

    @Test
    void getLessonById_found_delegatesToMapper() {
        var id = UUID.randomUUID();
        var subject = Subject.builder().id(UUID.randomUUID()).name("Math").build();
        var lesson = Lesson.builder().id(id).subject(subject).type(LessonType.LECTURE).orderIndex(1).build();
        var expectedResponse = lessonResponse(id);

        when(lessonRepository.findWithDetailsById(id)).thenReturn(Optional.of(lesson));
        when(gradingApi.getAssignmentsByLesson(id)).thenReturn(List.of());
        when(lessonMapper.toResponse(eq(lesson), any(), any())).thenReturn(expectedResponse);

        var result = service.getLessonById(id);

        assertThat(result).isEqualTo(expectedResponse);
        verify(lessonMapper).toResponse(eq(lesson), any(), any());
    }

    // -------------------------------------------------------------------------
    // setActive
    // -------------------------------------------------------------------------

    @Test
    void setActive_notFound_throwsResourceNotFoundException() {
        var id = UUID.randomUUID();
        when(lessonRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.setActive(id, true))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Lesson");
    }

    @Test
    void setActive_true_clearsOtherActiveLessonsOfSameSubjectAndType() {
        var id = UUID.randomUUID();
        var subjectId = UUID.randomUUID();
        var subject = Subject.builder().id(subjectId).name("Physics").build();
        var lesson = Lesson.builder().id(id).subject(subject).type(LessonType.PRACTICE).orderIndex(2).build();

        when(lessonRepository.findById(id)).thenReturn(Optional.of(lesson));
        when(lessonRepository.save(lesson)).thenReturn(lesson);
        // getLessonById called at end of setActive
        when(lessonRepository.findWithDetailsById(id)).thenReturn(Optional.of(lesson));
        when(gradingApi.getAssignmentsByLesson(id)).thenReturn(List.of());
        when(lessonMapper.toResponse(any(), any(), any())).thenReturn(lessonResponse(id));

        service.setActive(id, true);

        verify(lessonRepository).clearActiveForSubjectAndType(subjectId, LessonType.PRACTICE);
        verify(lessonRepository).flush();
        assertThat(lesson.isActive()).isTrue();
    }

    @Test
    void setActive_false_doesNotClearOtherLessons() {
        var id = UUID.randomUUID();
        var subject = Subject.builder().id(UUID.randomUUID()).name("History").build();
        var lesson = Lesson.builder().id(id).subject(subject).type(LessonType.LECTURE).orderIndex(1).active(true).build();

        when(lessonRepository.findById(id)).thenReturn(Optional.of(lesson));
        when(lessonRepository.save(lesson)).thenReturn(lesson);
        when(lessonRepository.findWithDetailsById(id)).thenReturn(Optional.of(lesson));
        when(gradingApi.getAssignmentsByLesson(id)).thenReturn(List.of());
        when(lessonMapper.toResponse(any(), any(), any())).thenReturn(lessonResponse(id));

        service.setActive(id, false);

        verify(lessonRepository, never()).clearActiveForSubjectAndType(any(), any());
        assertThat(lesson.isActive()).isFalse();
    }

    // -------------------------------------------------------------------------
    // deleteLesson
    // -------------------------------------------------------------------------

    @Test
    void deleteLesson_notFound_throwsResourceNotFoundException() {
        var id = UUID.randomUUID();
        when(lessonRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteLesson(id))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Lesson");
    }

    @Test
    void deleteLesson_found_archivesAndShiftsOrderIndex() {
        var id = UUID.randomUUID();
        var subjectId = UUID.randomUUID();
        var subject = Subject.builder().id(subjectId).name("Chemistry").build();
        var lesson = Lesson.builder()
            .id(id).subject(subject).type(LessonType.LECTURE).orderIndex(3).build();

        when(lessonRepository.findById(id)).thenReturn(Optional.of(lesson));
        when(lessonRepository.save(lesson)).thenReturn(lesson);

        service.deleteLesson(id);

        assertThat(lesson.isArchived()).isTrue();
        verify(lessonRepository).save(lesson);
        verify(lessonRepository).flush();
        verify(lessonRepository).shiftOrderIndexDown(subjectId, LessonType.LECTURE, 3);
    }

    // -------------------------------------------------------------------------
    // bulkCreate
    // -------------------------------------------------------------------------

    @Test
    void bulkCreate_subjectNotFound_throwsResourceNotFoundException() {
        var subjectId = UUID.randomUUID();
        var request = new BulkCreateLessonsRequest(subjectId, 1, 0);
        when(subjectRepository.findById(subjectId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.bulkCreate(request))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Subject");
    }

    @Test
    void bulkCreate_createsCorrectNumberOfLessons() {
        var subjectId = UUID.randomUUID();
        var subject = Subject.builder().id(subjectId).name("Biology").build();
        var request = new BulkCreateLessonsRequest(subjectId, 2, 1);

        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(subject));
        when(subjectRepository.getReferenceById(subjectId)).thenReturn(subject);
        // findMaxOrderIndex for each type
        when(lessonRepository.findMaxOrderIndex(subjectId, LessonType.LECTURE)).thenReturn(null);
        when(lessonRepository.findMaxOrderIndex(subjectId, LessonType.PRACTICE)).thenReturn(null);
        when(lessonRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));
        when(lessonMapper.toResponse(any(Lesson.class), any(), any()))
            .thenAnswer(inv -> lessonResponse(UUID.randomUUID()));

        var results = service.bulkCreate(request);

        assertThat(results).hasSize(3);
        verify(lessonRepository).saveAll(any());
    }

    @Test
    void bulkCreate_assignsDefaultTopicsWhenNoneProvided() {
        var subjectId = UUID.randomUUID();
        var subject = Subject.builder().id(subjectId).name("Physics").build();
        var request = new BulkCreateLessonsRequest(subjectId, 1, 1);

        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(subject));
        when(subjectRepository.getReferenceById(subjectId)).thenReturn(subject);
        when(lessonRepository.findMaxOrderIndex(eq(subjectId), any(LessonType.class))).thenReturn(null);
        when(lessonRepository.saveAll(any())).thenAnswer(inv -> {
            List<Lesson> saved = inv.getArgument(0);
            // Verify topics were set
            boolean hasLecture = saved.stream()
                .anyMatch(l -> l.getType() == LessonType.LECTURE && l.getTopic() != null && l.getTopic().startsWith("Лекция"));
            boolean hasPractice = saved.stream()
                .anyMatch(l -> l.getType() == LessonType.PRACTICE && l.getTopic() != null && l.getTopic().startsWith("Практика"));
            assertThat(hasLecture).isTrue();
            assertThat(hasPractice).isTrue();
            return saved;
        });
        when(lessonMapper.toResponse(any(Lesson.class), any(), any()))
            .thenReturn(lessonResponse(UUID.randomUUID()));

        service.bulkCreate(request);
    }

    // -------------------------------------------------------------------------
    // updateLesson
    // -------------------------------------------------------------------------

    @Test
    void updateLesson_notFound_throwsResourceNotFoundException() {
        var id = UUID.randomUUID();
        var request = new UpdateLessonRequest(null, null, null);
        when(lessonRepository.findWithDetailsById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateLesson(id, request))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Lesson");
    }

    @Test
    void updateLesson_nullSections_onlyReadsLesson() {
        var id = UUID.randomUUID();
        var subject = Subject.builder().id(UUID.randomUUID()).name("Art").build();
        var lesson = Lesson.builder().id(id).subject(subject).type(LessonType.LECTURE).orderIndex(1).build();
        var request = new UpdateLessonRequest(null, null, null);

        // First call in updateLesson, second call in getLessonById at the end
        when(lessonRepository.findWithDetailsById(id))
            .thenReturn(Optional.of(lesson))
            .thenReturn(Optional.of(lesson));
        when(gradingApi.getAssignmentsByLesson(id)).thenReturn(List.of());
        when(lessonMapper.toResponse(any(), any(), any())).thenReturn(lessonResponse(id));

        service.updateLesson(id, request);

        verify(lessonRepository, never()).save(any());
        verify(lessonScopesApi, never()).replaceScopesOfLesson(any(), any());
        verify(gradingApi, never()).updateAssignmentsOfLesson(any(), any());
    }

    // -------------------------------------------------------------------------
    // earliestStartedAt static helper
    // -------------------------------------------------------------------------

    @Test
    void earliestStartedAt_returnsNullWhenNoScopes() {
        var lesson = Lesson.builder()
            .id(UUID.randomUUID())
            .subject(Subject.builder().id(UUID.randomUUID()).build())
            .type(LessonType.LECTURE)
            .orderIndex(1)
            .build();

        assertThat(LessonService.earliestStartedAt(lesson)).isNull();
    }

    @Test
    void earliestStartedAt_returnsMinDate() {
        var lesson = Lesson.builder()
            .id(UUID.randomUUID())
            .subject(Subject.builder().id(UUID.randomUUID()).build())
            .type(LessonType.LECTURE)
            .orderIndex(1)
            .build();
        var scope1 = LessonScope.builder().lesson(lesson).startedAt(LocalDate.of(2025, 3, 10)).allGroups(true).build();
        var scope2 = LessonScope.builder().lesson(lesson).startedAt(LocalDate.of(2025, 1, 5)).allGroups(true).build();
        lesson.getScopes().add(scope1);
        lesson.getScopes().add(scope2);

        var earliest = LessonService.earliestStartedAt(lesson);

        assertThat(earliest).isEqualTo(LocalDate.of(2025, 1, 5));
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static LessonResponse lessonResponse(UUID id) {
        return new LessonResponse(id, UUID.randomUUID(), "Subject", LessonType.LECTURE, 1, "Topic", false,
            List.of(), List.of(), null, null);
    }
}
