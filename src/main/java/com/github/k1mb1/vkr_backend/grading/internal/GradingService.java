package com.github.k1mb1.vkr_backend.grading.internal;

import com.github.k1mb1.vkr_backend.common.error.ResourceNotFoundException;
import com.github.k1mb1.vkr_backend.grading.GradingApi;
import com.github.k1mb1.vkr_backend.grading.domain.Assignment;
import com.github.k1mb1.vkr_backend.grading.domain.Grade;
import com.github.k1mb1.vkr_backend.grading.web.filters.GradingFilter;
import com.github.k1mb1.vkr_backend.grading.web.requests.CreateAssignmentsRequest;
import com.github.k1mb1.vkr_backend.grading.web.requests.UpdateAssignmentRequest;
import com.github.k1mb1.vkr_backend.grading.web.requests.UpsertGradeRequest;
import com.github.k1mb1.vkr_backend.grading.web.responses.*;
import com.github.k1mb1.vkr_backend.lesson.LessonStudentsApi;
import com.github.k1mb1.vkr_backend.lesson.domain.Lesson;
import com.github.k1mb1.vkr_backend.lesson.domain.LessonScope;
import com.github.k1mb1.vkr_backend.lesson.internal.LessonRepository;
import com.github.k1mb1.vkr_backend.lesson.internal.LessonSpecifications;
import com.github.k1mb1.vkr_backend.student.domain.Student;
import com.github.k1mb1.vkr_backend.student.internal.StudentRepository;
import com.github.k1mb1.vkr_backend.subject.domain.PermissionScope;
import com.github.k1mb1.vkr_backend.subject.domain.TeacherSubjectPermission;
import com.github.k1mb1.vkr_backend.subject.internal.TeacherSubjectPermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class GradingService
    implements GradingApi {

    final GradeRepository gradeRepository;

    final AssignmentRepository assignmentRepository;

    final GradingMapper gradingMapper;

    final TeacherSubjectPermissionRepository permissionRepository;

    final LessonRepository lessonRepository;

    final StudentRepository studentRepository;

    final LessonStudentsApi lessonStudentsApi;

    static LocalDate earliestStartedAt(Lesson lesson) {
        return lesson.getScopes()
            .stream()
            .map(LessonScope::getStartedAt)
            .filter(Objects::nonNull)
            .min(Comparator.naturalOrder())
            .orElse(null);
    }

    @Override
    public GradingTableResponse getGradingTable(GradingFilter filter) {
        var permission = permissionRepository.findWithDetailsById(filter.permissionId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "TeacherSubjectPermission",
                filter.permissionId()
            ));

        var lessons = lessonRepository.findAll(LessonSpecifications.forPermission(permission))
            .stream()
            .sorted(Comparator.comparing(
                    GradingService::earliestStartedAt,
                    Comparator.nullsLast(Comparator.naturalOrder())
                )
                        .thenComparingInt(Lesson::getOrderIndex))
            .toList();

        var students = unionStudentsAcross(lessons);
        var audience = audienceOf(permission);

        return buildTable(audience, students, lessons);
    }

    private GradingTableResponse buildTable(
        List<GradingAudienceScope> audience,
        List<Student> students,
        List<Lesson> lessons
    ) {
        var studentIds = students.stream().map(Student::getId).toList();
        var lessonIds = lessons.stream().map(Lesson::getId).toList();

        var assignments = lessonIds.isEmpty()
                          ? List.<Assignment>of()
                          : assignmentRepository.findByLessonIdInOrderByLessonIdAscOrderAsc(
                              lessonIds);

        var grades = studentIds.isEmpty() || lessonIds.isEmpty()
                     ? List.<Grade>of()
                     : gradeRepository.findByLessonIdInAndStudentIdIn(lessonIds, studentIds);

        return new GradingTableResponse(
            audience,
            students.stream().map(gradingMapper::toTableStudent).toList(),
            lessons.stream().map(this::toGradingLesson).toList(),
            assignments.stream().map(gradingMapper::toAssignmentResponse).toList(),
            grades.stream().map(gradingMapper::toCell).toList()
        );
    }

    private GradingTableLesson toGradingLesson(Lesson lesson) {
        var scopes = lesson.getScopes()
            .stream()
            .sorted(Comparator.comparing(
                (LessonScope s) -> s.getStartedAt(),
                Comparator.nullsLast(Comparator.naturalOrder())
            ))
            .map(s -> new GradingTableLesson.Scope(
                s.getId(),
                s.getGroup() != null
                ? s.getGroup().getId()
                : null,
                s.getAllowedSubgroup() != null
                ? s.getAllowedSubgroup().getId()
                : null,
                s.getStartedAt(),
                s.isAllGroups()
            ))
            .toList();
        return new GradingTableLesson(
            lesson.getId(),
                                      lesson.getType(),
                                      lesson.getOrderIndex(),
                                      lesson.getTopic(),
                                      scopes
        );
    }

    @Transactional
    @Override
    public GradeCellResponse upsertGrade(UpsertGradeRequest request) {
        Assignment assignment = null;
        if (request.assignmentId() != null) {
            assignment = assignmentRepository.findById(request.assignmentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Assignment",
                    request.assignmentId()
                ));
            if (!assignment.getLesson().getId().equals(request.lessonId())) {
                throw new IllegalArgumentException("Assignment " + assignment.getId() + " does not belong to lesson " + request.lessonId());
            }
            if (request.score() > assignment.getMaxPoints()) {
                throw new IllegalArgumentException("Score " + request.score() + " exceeds assignment maxPoints " + assignment.getMaxPoints());
            }
        }

        var existing = request.assignmentId() == null
                       ? gradeRepository.findByStudentIdAndLessonIdAndAssignmentIsNull(
            request.studentId(),
            request.lessonId()
        )
                       : gradeRepository.findByStudentIdAndAssignmentId(
                           request.studentId(),
                           request.assignmentId()
                       );

        final Assignment assignmentRef = assignment;
        var grade = existing.orElseGet(() -> Grade.builder()
            .student(studentRepository.getReferenceById(request.studentId()))
            .lesson(lessonRepository.getReferenceById(request.lessonId()))
            .assignment(assignmentRef)
            .build());

        grade.setScore(request.score());
        grade.setComment(request.comment());

        return gradingMapper.toCell(gradeRepository.save(grade));
    }

    @Override
    public List<AssignmentResponse> getAssignmentsByLesson(UUID lessonId) {
        return assignmentRepository.findByLessonIdInOrderByLessonIdAscOrderAsc(List.of(lessonId))
            .stream()
            .map(gradingMapper::toAssignmentResponse)
            .toList();
    }

    @Transactional
    @Override
    public List<AssignmentResponse> createAssignments(CreateAssignmentsRequest request) {
        if (assignmentRepository.existsByLessonId(request.lessonId())) {
            throw new IllegalStateException("Lesson " + request.lessonId() + " already has assignments. " + "Use PUT /api/assignments/{id} to update individual assignments.");
        }
        var lessonRef = lessonRepository.getReferenceById(request.lessonId());
        var items = request.items();
        var assignments = new ArrayList<Assignment>(items.size());
        for (int i = 0; i < items.size(); i++) {
            var item = items.get(i);
            assignments.add(Assignment.builder()
                                .lesson(lessonRef)
                                .order(i + 1)
                                .maxPoints(item.maxPoints())
                                .required(item.required())
                                .build());
        }
        return assignmentRepository.saveAll(assignments)
            .stream()
            .map(gradingMapper::toAssignmentResponse)
            .toList();
    }

    @Transactional
    @Override
    public AssignmentResponse updateAssignment(UUID id, UpdateAssignmentRequest request) {
        var assignment = assignmentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Assignment", id));
        assignment.setOrder(request.order());
        assignment.setMaxPoints(request.maxPoints());
        assignment.setRequired(request.required());
        return gradingMapper.toAssignmentResponse(assignmentRepository.save(assignment));
    }

    @Transactional
    @Override
    public void deleteAssignment(UUID id) {
        if (!assignmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Assignment", id);
        }
        assignmentRepository.deleteById(id);
    }

    private List<Student> unionStudentsAcross(List<Lesson> lessons) {
        var seen = new LinkedHashMap<UUID, Student>();
        for (var lesson : lessons) {
            for (var s : lessonStudentsApi.studentsOf(lesson)) {
                seen.putIfAbsent(s.getId(), s);
            }
        }
        var result = new ArrayList<>(seen.values());
        result.sort(Comparator.comparing(Student::getUsername));
        return result;
    }

    private List<GradingAudienceScope> audienceOf(TeacherSubjectPermission permission) {
        if (permission.isAllPermissions()) {
            return List.of();
        }
        return permission.getScopes()
            .stream()
            .sorted(Comparator.comparing((PermissionScope s) -> s.getGroup().getName())
                        .thenComparing(s -> s.getAllowedSubgroup() == null
                                            ? -1
                                            : s.getAllowedSubgroup().getIndex()))
            .map(s -> new GradingAudienceScope(
                s.getGroup().getId(),
                s.getGroup().getName(),
                s.getAllowedSubgroup() != null
                ? s.getAllowedSubgroup().getId()
                : null,
                s.getAllowedSubgroup() != null
                ? s.getAllowedSubgroup().getIndex()
                : null
            ))
            .toList();
    }
}
