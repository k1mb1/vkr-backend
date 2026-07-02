package com.github.k1mb1.vkr_backend.grading.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.k1mb1.vkr_backend.common.error.ConflictException;
import com.github.k1mb1.vkr_backend.common.error.ResourceNotFoundException;
import com.github.k1mb1.vkr_backend.common.security.AuthorizationService;
import com.github.k1mb1.vkr_backend.grading.domain.Assignment;
import com.github.k1mb1.vkr_backend.grading.domain.AssignmentAdmissionMode;
import com.github.k1mb1.vkr_backend.grading.web.requests.BulkUpdateAssignmentsRequest;
import com.github.k1mb1.vkr_backend.grading.web.requests.BulkUpsertGradesRequest;
import com.github.k1mb1.vkr_backend.grading.web.requests.CreateAssignmentsRequest;
import com.github.k1mb1.vkr_backend.grading.web.requests.UpsertGradeRequest;
import com.github.k1mb1.vkr_backend.grading.web.responses.AssignmentResponse;
import com.github.k1mb1.vkr_backend.lesson.domain.Lesson;
import com.github.k1mb1.vkr_backend.lesson.internal.LessonRepository;
import com.github.k1mb1.vkr_backend.lesson.internal.LessonResolver;
import com.github.k1mb1.vkr_backend.student.internal.StudentRepository;
import com.github.k1mb1.vkr_backend.subject.internal.SubjectMapper;
import com.github.k1mb1.vkr_backend.subject.internal.TeacherSubjectPermissionRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GradingServiceTest {

    @Mock GradeRepository gradeRepository;
    @Mock AssignmentRepository assignmentRepository;
    @Mock GradingMapper gradingMapper;
    @Mock TeacherSubjectPermissionRepository permissionRepository;
    @Mock SubjectMapper subjectMapper;
    @Mock LessonRepository lessonRepository;
    @Mock LessonResolver lessonResolver;
    @Mock StudentRepository studentRepository;
    @Mock com.github.k1mb1.vkr_backend.attendance.AttendanceApi attendanceApi;
    @Mock com.github.k1mb1.vkr_backend.lesson.LessonStudentsApi lessonStudentsApi;
    @Mock AuthorizationService authz;

    @InjectMocks
    GradingService service;

    private UpsertGradeRequest grade(UUID studentId, UUID lessonId, UUID assignmentId, int score) {
        return new UpsertGradeRequest(studentId, lessonId, assignmentId, score, null);
    }

    // ---- upsertGrades validation ----

    @Test
    void upsertGradesRejectsDuplicateTriple() {
        var s = UUID.randomUUID();
        var l = UUID.randomUUID();
        var a = UUID.randomUUID();
        var request = new BulkUpsertGradesRequest(List.of(grade(s, l, a, 5), grade(s, l, a, 6)));

        assertThatThrownBy(() -> service.upsertGrades(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Duplicate");
    }

    @Test
    void upsertGradesThrowsWhenAssignmentNotFound() {
        var s = UUID.randomUUID();
        var l = UUID.randomUUID();
        var a = UUID.randomUUID();
        when(assignmentRepository.findAllById(List.of(a))).thenReturn(List.of());

        var request = new BulkUpsertGradesRequest(List.of(grade(s, l, a, 5)));

        assertThatThrownBy(() -> service.upsertGrades(request))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void upsertGradesRejectsAssignmentFromAnotherLesson() {
        var s = UUID.randomUUID();
        var l = UUID.randomUUID();
        var a = UUID.randomUUID();
        var assignment = Assignment.builder()
            .id(a).maxPoints(10)
            .lesson(Lesson.builder().id(UUID.randomUUID()).build())
            .build();
        when(assignmentRepository.findAllById(List.of(a))).thenReturn(List.of(assignment));

        var request = new BulkUpsertGradesRequest(List.of(grade(s, l, a, 5)));

        assertThatThrownBy(() -> service.upsertGrades(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("does not belong to lesson");
    }

    @Test
    void upsertGradesRejectsScoreAboveMaxPoints() {
        var s = UUID.randomUUID();
        var l = UUID.randomUUID();
        var a = UUID.randomUUID();
        var assignment = Assignment.builder()
            .id(a).maxPoints(10)
            .lesson(Lesson.builder().id(l).build())
            .build();
        when(assignmentRepository.findAllById(List.of(a))).thenReturn(List.of(assignment));

        var request = new BulkUpsertGradesRequest(List.of(grade(s, l, a, 20)));

        assertThatThrownBy(() -> service.upsertGrades(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("exceeds assignment maxPoints");
    }

    // ---- createAssignments ----

    @Test
    void createAssignmentsThrowsConflictWhenLessonAlreadyHasAssignments() {
        var lessonId = UUID.randomUUID();
        when(assignmentRepository.existsByLessonId(lessonId)).thenReturn(true);

        var request = new CreateAssignmentsRequest(lessonId, List.of(
            new CreateAssignmentsRequest.Item(10, true, AssignmentAdmissionMode.NONE, null, null)
        ));

        assertThatThrownBy(() -> service.createAssignments(request))
            .isInstanceOf(ConflictException.class);
    }

    @Test
    void createAssignmentsRejectsPassFailWithMinScore() {
        var lessonId = UUID.randomUUID();
        when(assignmentRepository.existsByLessonId(lessonId)).thenReturn(false);
        when(lessonRepository.getReferenceById(lessonId))
            .thenReturn(Lesson.builder().id(lessonId).build());

        var request = new CreateAssignmentsRequest(lessonId, List.of(
            new CreateAssignmentsRequest.Item(10, true, AssignmentAdmissionMode.PASS_FAIL, 5, null)
        ));

        assertThatThrownBy(() -> service.createAssignments(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("PASS_FAIL");
    }

    @Test
    void createAssignmentsAssignsSequentialOrder() {
        var lessonId = UUID.randomUUID();
        when(assignmentRepository.existsByLessonId(lessonId)).thenReturn(false);
        when(lessonRepository.getReferenceById(lessonId))
            .thenReturn(Lesson.builder().id(lessonId).build());
        when(assignmentRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(gradingMapper.toAssignmentResponse(any())).thenReturn(mock(AssignmentResponse.class));

        var request = new CreateAssignmentsRequest(lessonId, List.of(
            new CreateAssignmentsRequest.Item(10, true, AssignmentAdmissionMode.NONE, null, null),
            new CreateAssignmentsRequest.Item(20, false, AssignmentAdmissionMode.NONE, null, null)
        ));

        service.createAssignments(request);

        var captor = ArgumentCaptor.forClass(List.class);
        verify(assignmentRepository).saveAll(captor.capture());
        List<Assignment> saved = captor.getValue();
        assertThat(saved).extracting(Assignment::getOrder).containsExactly(1, 2);
    }

    // ---- updateAssignmentsOfLesson ----

    @Test
    void updateAssignmentsRejectsDuplicateId() {
        var lessonId = UUID.randomUUID();
        var dupId = UUID.randomUUID();
        var request = new BulkUpdateAssignmentsRequest(List.of(
            new BulkUpdateAssignmentsRequest.Item(dupId, 1, 10, true, AssignmentAdmissionMode.NONE, null, null),
            new BulkUpdateAssignmentsRequest.Item(dupId, 2, 10, true, AssignmentAdmissionMode.NONE, null, null)
        ));

        assertThatThrownBy(() -> service.updateAssignmentsOfLesson(lessonId, request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Duplicate assignment id");
    }

    // ---- getAssignmentsByLessons ----

    @Test
    void getAssignmentsByLessonsGroupsByLesson() {
        var l1 = UUID.randomUUID();
        var l2 = UUID.randomUUID();
        var assignment = Assignment.builder()
            .id(UUID.randomUUID()).lesson(Lesson.builder().id(l1).build()).build();
        when(assignmentRepository.findByLessonIdInOrderByLessonIdAscOrderAsc(List.of(l1, l2)))
            .thenReturn(List.of(assignment));
        when(gradingMapper.toAssignmentResponse(assignment)).thenReturn(mock(AssignmentResponse.class));

        var result = service.getAssignmentsByLessons(List.of(l1, l2));

        assertThat(result.get(l1)).hasSize(1);
        assertThat(result.get(l2)).isEmpty();
    }

    @Test
    void getAssignmentsByLessonsEmptyForEmptyInput() {
        assertThat(service.getAssignmentsByLessons(List.of())).isEmpty();
    }

    // ---- deleteAssignment ----

    @Test
    void deleteAssignmentThrowsWhenMissing() {
        var id = UUID.randomUUID();
        when(assignmentRepository.findSubjectIdById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteAssignment(id))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteAssignmentDeniedWhenNoSubjectAccess() {
        var id = UUID.randomUUID();
        var subjectId = UUID.randomUUID();
        when(assignmentRepository.findSubjectIdById(id)).thenReturn(Optional.of(subjectId));
        when(authz.canAccessSubject(subjectId)).thenReturn(false);

        assertThatThrownBy(() -> service.deleteAssignment(id))
            .isInstanceOf(org.springframework.security.access.AccessDeniedException.class);
    }

    @Test
    void deleteAssignmentDeletesWhenAllowed() {
        var id = UUID.randomUUID();
        var subjectId = UUID.randomUUID();
        when(assignmentRepository.findSubjectIdById(id)).thenReturn(Optional.of(subjectId));
        when(authz.canAccessSubject(subjectId)).thenReturn(true);

        service.deleteAssignment(id);

        verify(assignmentRepository).deleteById(id);
    }

    // ---- getGradingTable ----

    @Test
    void getGradingTableThrowsWhenPermissionMissing() {
        var permissionId = UUID.randomUUID();
        when(permissionRepository.findWithDetailsById(permissionId)).thenReturn(Optional.empty());

        var filter = com.github.k1mb1.vkr_backend.grading.web.filters.GradingFilter.builder()
            .permissionId(permissionId).build();

        assertThatThrownBy(() -> service.getGradingTable(filter))
            .isInstanceOf(ResourceNotFoundException.class);
    }
}
