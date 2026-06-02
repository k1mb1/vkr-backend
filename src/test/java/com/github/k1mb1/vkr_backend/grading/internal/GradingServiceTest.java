package com.github.k1mb1.vkr_backend.grading.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.k1mb1.vkr_backend.attendance.AttendanceApi;
import com.github.k1mb1.vkr_backend.common.error.ResourceNotFoundException;
import com.github.k1mb1.vkr_backend.grading.domain.Assignment;
import com.github.k1mb1.vkr_backend.grading.domain.Grade;
import com.github.k1mb1.vkr_backend.grading.web.requests.BulkUpdateAssignmentsRequest;
import com.github.k1mb1.vkr_backend.grading.web.requests.BulkUpsertGradesRequest;
import com.github.k1mb1.vkr_backend.grading.web.requests.CreateAssignmentsRequest;
import com.github.k1mb1.vkr_backend.grading.web.requests.UpsertGradeRequest;
import com.github.k1mb1.vkr_backend.grading.web.responses.AssignmentResponse;
import com.github.k1mb1.vkr_backend.grading.web.responses.GradeCellResponse;
import com.github.k1mb1.vkr_backend.lesson.LessonStudentsApi;
import com.github.k1mb1.vkr_backend.lesson.domain.Lesson;
import com.github.k1mb1.vkr_backend.lesson.domain.LessonScope;
import com.github.k1mb1.vkr_backend.lesson.domain.LessonType;
import com.github.k1mb1.vkr_backend.lesson.internal.LessonRepository;
import com.github.k1mb1.vkr_backend.lesson.internal.LessonScopeRepository;
import com.github.k1mb1.vkr_backend.student.domain.Student;
import com.github.k1mb1.vkr_backend.student.internal.StudentRepository;
import com.github.k1mb1.vkr_backend.subject.domain.Subject;
import com.github.k1mb1.vkr_backend.subject.internal.TeacherSubjectPermissionRepository;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GradingServiceTest {

    @Mock GradeRepository gradeRepository;
    @Mock AssignmentRepository assignmentRepository;
    @Mock GradingMapper gradingMapper;
    @Mock TeacherSubjectPermissionRepository permissionRepository;
    @Mock LessonRepository lessonRepository;
    @Mock LessonScopeRepository lessonScopeRepository;
    @Mock StudentRepository studentRepository;
    @Mock AttendanceApi attendanceApi;
    @Mock LessonStudentsApi lessonStudentsApi;

    @InjectMocks GradingService service;

    // =========================================================================
    // earliestStartedAt — pure static, no mocks
    // =========================================================================

    @Test
    void earliestStartedAt_noScopes_returnsNull() {
        var lesson = Lesson.builder()
            .id(UUID.randomUUID())
            .subject(Subject.builder().id(UUID.randomUUID()).build())
            .type(LessonType.LECTURE)
            .orderIndex(1)
            .build();

        assertThat(GradingService.earliestStartedAt(lesson)).isNull();
    }

    @Test
    void earliestStartedAt_allScopesNullDates_returnsNull() {
        var lesson = Lesson.builder()
            .id(UUID.randomUUID())
            .subject(Subject.builder().id(UUID.randomUUID()).build())
            .type(LessonType.LECTURE)
            .orderIndex(1)
            .build();
        // LessonScope.startedAt column is non-null in the schema but we can build it null in tests
        var scope = LessonScope.builder()
            .lesson(lesson)
            .startedAt(null)
            .allGroups(true)
            .build();
        lesson.getScopes().add(scope);

        assertThat(GradingService.earliestStartedAt(lesson)).isNull();
    }

    @Test
    void earliestStartedAt_multipleScopesDates_returnsMin() {
        var lesson = Lesson.builder()
            .id(UUID.randomUUID())
            .subject(Subject.builder().id(UUID.randomUUID()).build())
            .type(LessonType.PRACTICE)
            .orderIndex(2)
            .build();
        lesson.getScopes().add(LessonScope.builder().lesson(lesson)
            .startedAt(LocalDate.of(2025, 6, 15)).allGroups(true).build());
        lesson.getScopes().add(LessonScope.builder().lesson(lesson)
            .startedAt(LocalDate.of(2025, 3, 1)).allGroups(true).build());
        lesson.getScopes().add(LessonScope.builder().lesson(lesson)
            .startedAt(LocalDate.of(2025, 9, 20)).allGroups(true).build());

        assertThat(GradingService.earliestStartedAt(lesson)).isEqualTo(LocalDate.of(2025, 3, 1));
    }

    @Test
    void earliestStartedAt_singleScope_returnsThatDate() {
        var lesson = Lesson.builder()
            .id(UUID.randomUUID())
            .subject(Subject.builder().id(UUID.randomUUID()).build())
            .type(LessonType.LECTURE)
            .orderIndex(1)
            .build();
        lesson.getScopes().add(LessonScope.builder().lesson(lesson)
            .startedAt(LocalDate.of(2025, 5, 10)).allGroups(true).build());

        assertThat(GradingService.earliestStartedAt(lesson)).isEqualTo(LocalDate.of(2025, 5, 10));
    }

    // =========================================================================
    // upsertGrades
    // =========================================================================

    @Test
    void upsertGrades_duplicateKey_throwsIllegalArgument() {
        var studentId = UUID.randomUUID();
        var lessonId = UUID.randomUUID();
        var assignmentId = UUID.randomUUID();

        var request = new BulkUpsertGradesRequest(List.of(
            new UpsertGradeRequest(studentId, lessonId, assignmentId, 5, null),
            new UpsertGradeRequest(studentId, lessonId, assignmentId, 3, null)
        ));

        assertThatThrownBy(() -> service.upsertGrades(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Duplicate");
    }

    @Test
    void upsertGrades_missingAssignment_throwsResourceNotFoundException() {
        var studentId = UUID.randomUUID();
        var lessonId = UUID.randomUUID();
        var assignmentId = UUID.randomUUID();

        var request = new BulkUpsertGradesRequest(List.of(
            new UpsertGradeRequest(studentId, lessonId, assignmentId, 5, null)
        ));

        when(assignmentRepository.findAllById(any())).thenReturn(List.of());

        assertThatThrownBy(() -> service.upsertGrades(request))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Assignment");
    }

    @Test
    void upsertGrades_assignmentNotBelongingToLesson_throwsIllegalArgument() {
        var studentId = UUID.randomUUID();
        var requestedLessonId = UUID.randomUUID();
        var actualLessonId = UUID.randomUUID();
        var assignmentId = UUID.randomUUID();

        // Build a lesson that the assignment belongs to (different from requested)
        var subject = Subject.builder().id(UUID.randomUUID()).build();
        var actualLesson = Lesson.builder()
            .id(actualLessonId)
            .subject(subject)
            .type(LessonType.LECTURE)
            .orderIndex(1)
            .build();
        var assignment = Assignment.builder()
            .id(assignmentId)
            .lesson(actualLesson)
            .order(1)
            .maxPoints(10)
            .required(true)
            .build();

        var request = new BulkUpsertGradesRequest(List.of(
            new UpsertGradeRequest(studentId, requestedLessonId, assignmentId, 5, null)
        ));

        when(assignmentRepository.findAllById(any())).thenReturn(List.of(assignment));

        assertThatThrownBy(() -> service.upsertGrades(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("does not belong to lesson");
    }

    @Test
    void upsertGrades_scoreExceedsMaxPoints_throwsIllegalArgument() {
        var studentId = UUID.randomUUID();
        var lessonId = UUID.randomUUID();
        var assignmentId = UUID.randomUUID();

        var subject = Subject.builder().id(UUID.randomUUID()).build();
        var lesson = Lesson.builder()
            .id(lessonId)
            .subject(subject)
            .type(LessonType.LECTURE)
            .orderIndex(1)
            .build();
        var assignment = Assignment.builder()
            .id(assignmentId)
            .lesson(lesson)
            .order(1)
            .maxPoints(10)
            .required(true)
            .build();

        var request = new BulkUpsertGradesRequest(List.of(
            new UpsertGradeRequest(studentId, lessonId, assignmentId, 15, null)
        ));

        when(assignmentRepository.findAllById(any())).thenReturn(List.of(assignment));

        assertThatThrownBy(() -> service.upsertGrades(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("exceeds assignment maxPoints");
    }

    @Test
    void upsertGrades_happyPath_savesAndReturnsCells() {
        var studentId = UUID.randomUUID();
        var lessonId = UUID.randomUUID();
        var assignmentId = UUID.randomUUID();

        var subject = Subject.builder().id(UUID.randomUUID()).build();
        var lesson = Lesson.builder()
            .id(lessonId)
            .subject(subject)
            .type(LessonType.LECTURE)
            .orderIndex(1)
            .build();
        var assignment = Assignment.builder()
            .id(assignmentId)
            .lesson(lesson)
            .order(1)
            .maxPoints(10)
            .required(false)
            .build();

        var request = new BulkUpsertGradesRequest(List.of(
            new UpsertGradeRequest(studentId, lessonId, assignmentId, 8, "Good")
        ));

        when(assignmentRepository.findAllById(any())).thenReturn(List.of(assignment));
        when(gradeRepository.findByLessonIdInAndStudentIdIn(any(), any())).thenReturn(List.of());
        when(lessonRepository.findBySubjectIdAndTypeAndActiveTrue(any(), any())).thenReturn(Optional.empty());
        when(studentRepository.getReferenceById(studentId)).thenReturn(
            Student.builder().id(studentId).username("alice").build()
        );
        when(lessonRepository.getReferenceById(lessonId)).thenReturn(lesson);
        when(gradeRepository.saveAll(any())).thenAnswer(i -> i.getArgument(0));

        var expected = new GradeCellResponse(UUID.randomUUID(), studentId, lessonId, assignmentId, null, null, 8, "Good");
        when(gradingMapper.toCell(any(Grade.class))).thenReturn(expected);

        var result = service.upsertGrades(request);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(expected);
        verify(gradeRepository).saveAll(any());
    }

    // =========================================================================
    // createAssignments
    // =========================================================================

    @Test
    void createAssignments_existingAssignments_throwsIllegalState() {
        var lessonId = UUID.randomUUID();
        var request = new CreateAssignmentsRequest(lessonId, List.of(
            new CreateAssignmentsRequest.Item(10, true)
        ));

        when(assignmentRepository.existsByLessonId(lessonId)).thenReturn(true);

        assertThatThrownBy(() -> service.createAssignments(request))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("already has assignments");
    }

    @Test
    void createAssignments_happyPath_createsOrderedAssignments() {
        var lessonId = UUID.randomUUID();
        var lesson = Lesson.builder()
            .id(lessonId)
            .subject(Subject.builder().id(UUID.randomUUID()).build())
            .type(LessonType.LECTURE)
            .orderIndex(1)
            .build();
        var request = new CreateAssignmentsRequest(lessonId, List.of(
            new CreateAssignmentsRequest.Item(10, true),
            new CreateAssignmentsRequest.Item(20, false),
            new CreateAssignmentsRequest.Item(5, true)
        ));

        when(assignmentRepository.existsByLessonId(lessonId)).thenReturn(false);
        when(lessonRepository.getReferenceById(lessonId)).thenReturn(lesson);
        when(assignmentRepository.saveAll(any())).thenAnswer(inv -> {
            List<Assignment> saved = inv.getArgument(0);
            assertThat(saved).hasSize(3);
            assertThat(saved.get(0).getOrder()).isEqualTo(1);
            assertThat(saved.get(1).getOrder()).isEqualTo(2);
            assertThat(saved.get(2).getOrder()).isEqualTo(3);
            assertThat(saved.get(1).getMaxPoints()).isEqualTo(20);
            return saved;
        });
        var resp = new AssignmentResponse(UUID.randomUUID(), lessonId, 1, 10, true);
        when(gradingMapper.toAssignmentResponse(any(Assignment.class))).thenReturn(resp);

        var result = service.createAssignments(request);

        assertThat(result).hasSize(3);
        verify(assignmentRepository).saveAll(any());
    }

    // =========================================================================
    // updateAssignmentsOfLesson
    // =========================================================================

    @Test
    void updateAssignmentsOfLesson_duplicateIdInRequest_throwsIllegalArgument() {
        var lessonId = UUID.randomUUID();
        var assignmentId = UUID.randomUUID();
        var request = new BulkUpdateAssignmentsRequest(List.of(
            new BulkUpdateAssignmentsRequest.Item(assignmentId, 1, 10, true),
            new BulkUpdateAssignmentsRequest.Item(assignmentId, 2, 10, false)
        ));

        assertThatThrownBy(() -> service.updateAssignmentsOfLesson(lessonId, request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Duplicate assignment id");
    }

    @Test
    void updateAssignmentsOfLesson_missingAssignment_throwsResourceNotFoundException() {
        var lessonId = UUID.randomUUID();
        var assignmentId = UUID.randomUUID();
        var request = new BulkUpdateAssignmentsRequest(List.of(
            new BulkUpdateAssignmentsRequest.Item(assignmentId, 1, 10, true)
        ));

        when(assignmentRepository.findAllById(any())).thenReturn(List.of());

        assertThatThrownBy(() -> service.updateAssignmentsOfLesson(lessonId, request))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Assignment");
    }

    @Test
    void updateAssignmentsOfLesson_assignmentNotInLesson_throwsIllegalArgument() {
        var lessonId = UUID.randomUUID();
        var differentLessonId = UUID.randomUUID();
        var assignmentId = UUID.randomUUID();

        var differentLesson = Lesson.builder()
            .id(differentLessonId)
            .subject(Subject.builder().id(UUID.randomUUID()).build())
            .type(LessonType.LECTURE)
            .orderIndex(1)
            .build();
        var assignment = Assignment.builder()
            .id(assignmentId)
            .lesson(differentLesson)
            .order(1)
            .maxPoints(10)
            .required(true)
            .build();

        var request = new BulkUpdateAssignmentsRequest(List.of(
            new BulkUpdateAssignmentsRequest.Item(assignmentId, 1, 10, true)
        ));

        when(assignmentRepository.findAllById(any())).thenReturn(List.of(assignment));

        assertThatThrownBy(() -> service.updateAssignmentsOfLesson(lessonId, request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("does not belong to lesson");
    }

    @Test
    void updateAssignmentsOfLesson_duplicateFinalOrder_throwsIllegalArgument() {
        var lessonId = UUID.randomUUID();
        var lesson = Lesson.builder()
            .id(lessonId)
            .subject(Subject.builder().id(UUID.randomUUID()).build())
            .type(LessonType.LECTURE)
            .orderIndex(1)
            .build();
        var assignmentId1 = UUID.randomUUID();
        var assignmentId2 = UUID.randomUUID();

        var a1 = Assignment.builder().id(assignmentId1).lesson(lesson).order(1).maxPoints(10).required(true).build();
        var a2 = Assignment.builder().id(assignmentId2).lesson(lesson).order(2).maxPoints(10).required(false).build();

        // Both items request order=1 → duplicate
        var request = new BulkUpdateAssignmentsRequest(List.of(
            new BulkUpdateAssignmentsRequest.Item(assignmentId1, 1, 10, true),
            new BulkUpdateAssignmentsRequest.Item(assignmentId2, 1, 10, false)
        ));

        when(assignmentRepository.findAllById(any())).thenReturn(List.of(a1, a2));
        when(assignmentRepository.findByLessonIdInOrderByLessonIdAscOrderAsc(any())).thenReturn(List.of(a1, a2));

        assertThatThrownBy(() -> service.updateAssignmentsOfLesson(lessonId, request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Duplicate order");
    }

    @Test
    void updateAssignmentsOfLesson_happyPath_updatesAndReturnsOrdered() {
        var lessonId = UUID.randomUUID();
        var lesson = Lesson.builder()
            .id(lessonId)
            .subject(Subject.builder().id(UUID.randomUUID()).build())
            .type(LessonType.LECTURE)
            .orderIndex(1)
            .build();
        var assignmentId1 = UUID.randomUUID();
        var assignmentId2 = UUID.randomUUID();

        var a1 = Assignment.builder().id(assignmentId1).lesson(lesson).order(1).maxPoints(10).required(true).build();
        var a2 = Assignment.builder().id(assignmentId2).lesson(lesson).order(2).maxPoints(20).required(false).build();

        var request = new BulkUpdateAssignmentsRequest(List.of(
            new BulkUpdateAssignmentsRequest.Item(assignmentId1, 2, 15, true),
            new BulkUpdateAssignmentsRequest.Item(assignmentId2, 1, 25, false)
        ));

        when(assignmentRepository.findAllById(any())).thenReturn(List.of(a1, a2));
        when(assignmentRepository.findByLessonIdInOrderByLessonIdAscOrderAsc(any())).thenReturn(List.of(a1, a2));
        when(assignmentRepository.saveAllAndFlush(any())).thenReturn(List.of(a1, a2));
        when(assignmentRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

        var resp1 = new AssignmentResponse(assignmentId1, lessonId, 2, 15, true);
        var resp2 = new AssignmentResponse(assignmentId2, lessonId, 1, 25, false);
        when(gradingMapper.toAssignmentResponse(a1)).thenReturn(resp1);
        when(gradingMapper.toAssignmentResponse(a2)).thenReturn(resp2);

        var result = service.updateAssignmentsOfLesson(lessonId, request);

        assertThat(result).hasSize(2);
    }

    // =========================================================================
    // deleteAssignment
    // =========================================================================

    @Test
    void deleteAssignment_notExists_throwsResourceNotFoundException() {
        var id = UUID.randomUUID();
        when(assignmentRepository.existsById(id)).thenReturn(false);

        assertThatThrownBy(() -> service.deleteAssignment(id))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Assignment");
    }

    @Test
    void deleteAssignment_exists_callsDelete() {
        var id = UUID.randomUUID();
        when(assignmentRepository.existsById(id)).thenReturn(true);

        service.deleteAssignment(id);

        verify(assignmentRepository).deleteById(id);
    }

    // =========================================================================
    // getAssignmentsByLessons
    // =========================================================================

    @Test
    void getAssignmentsByLessons_emptyInput_returnsEmptyMap() {
        var result = service.getAssignmentsByLessons(List.of());

        assertThat(result).isEmpty();
    }

    @Test
    void getAssignmentsByLessons_groupsByLessonId() {
        var lessonId1 = UUID.randomUUID();
        var lessonId2 = UUID.randomUUID();
        var assignmentId1 = UUID.randomUUID();
        var assignmentId2 = UUID.randomUUID();
        var assignmentId3 = UUID.randomUUID();

        var lesson1 = Lesson.builder()
            .id(lessonId1)
            .subject(Subject.builder().id(UUID.randomUUID()).build())
            .type(LessonType.LECTURE)
            .orderIndex(1)
            .build();
        var lesson2 = Lesson.builder()
            .id(lessonId2)
            .subject(Subject.builder().id(UUID.randomUUID()).build())
            .type(LessonType.PRACTICE)
            .orderIndex(1)
            .build();

        var a1 = Assignment.builder().id(assignmentId1).lesson(lesson1).order(1).maxPoints(10).required(true).build();
        var a2 = Assignment.builder().id(assignmentId2).lesson(lesson1).order(2).maxPoints(20).required(false).build();
        var a3 = Assignment.builder().id(assignmentId3).lesson(lesson2).order(1).maxPoints(15).required(true).build();

        when(assignmentRepository.findByLessonIdInOrderByLessonIdAscOrderAsc(any())).thenReturn(List.of(a1, a2, a3));

        var resp1 = new AssignmentResponse(assignmentId1, lessonId1, 1, 10, true);
        var resp2 = new AssignmentResponse(assignmentId2, lessonId1, 2, 20, false);
        var resp3 = new AssignmentResponse(assignmentId3, lessonId2, 1, 15, true);
        when(gradingMapper.toAssignmentResponse(a1)).thenReturn(resp1);
        when(gradingMapper.toAssignmentResponse(a2)).thenReturn(resp2);
        when(gradingMapper.toAssignmentResponse(a3)).thenReturn(resp3);

        var result = service.getAssignmentsByLessons(List.of(lessonId1, lessonId2));

        assertThat(result).containsOnlyKeys(lessonId1, lessonId2);
        assertThat(result.get(lessonId1)).hasSize(2);
        assertThat(result.get(lessonId2)).hasSize(1);
        assertThat(result.get(lessonId2).get(0).id()).isEqualTo(assignmentId3);
    }
}
