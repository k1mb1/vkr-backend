package com.github.k1mb1.vkr_backend.grading.internal;

import com.github.k1mb1.vkr_backend.common.error.ResourceNotFoundException;
import com.github.k1mb1.vkr_backend.grading.GradingApi;
import com.github.k1mb1.vkr_backend.grading.domain.Assignment;
import com.github.k1mb1.vkr_backend.grading.domain.Grade;
import com.github.k1mb1.vkr_backend.grading.web.filters.GradingFilter;
import com.github.k1mb1.vkr_backend.grading.web.requests.BulkUpdateAssignmentsRequest;
import com.github.k1mb1.vkr_backend.grading.web.requests.BulkUpsertGradesRequest;
import com.github.k1mb1.vkr_backend.grading.web.requests.CreateAssignmentsRequest;
import com.github.k1mb1.vkr_backend.grading.web.requests.UpsertGradeRequest;
import com.github.k1mb1.vkr_backend.grading.web.responses.*;
import com.github.k1mb1.vkr_backend.lesson.LessonStudentsApi;
import com.github.k1mb1.vkr_backend.lesson.domain.Lesson;
import com.github.k1mb1.vkr_backend.lesson.domain.LessonScope;
import com.github.k1mb1.vkr_backend.lesson.internal.LessonRepository;
import com.github.k1mb1.vkr_backend.lesson.internal.LessonScopeRepository;
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

    final LessonScopeRepository lessonScopeRepository;

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

        var lessons = resolveLessons(permission, filter).stream()
            .sorted(Comparator.comparing(
                    GradingService::earliestStartedAt,
                    Comparator.nullsLast(Comparator.naturalOrder())
                )
                        .thenComparingInt(Lesson::getOrderIndex))
            .toList();

        var visibleScopesByLesson = new java.util.LinkedHashMap<Lesson, List<LessonScope>>();
        for (var lesson : lessons) {
            visibleScopesByLesson.put(
                lesson,
                LessonSpecifications.visibleScopes(lesson, permission)
            );
        }

        var students = unionStudentsAcrossScopes(visibleScopesByLesson.values());
        var audience = audienceOf(permission);

        return buildTable(audience, students, visibleScopesByLesson);
    }

    private List<Lesson> resolveLessons(
        TeacherSubjectPermission permission,
        GradingFilter filter
    ) {
        if (filter.lessonScopeId() != null) {
            var scope = lessonScopeRepository.findById(filter.lessonScopeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "LessonScope",
                    filter.lessonScopeId()
                ));
            assertSameSubject(scope.getLesson(), permission);
            assertLessonMatch(scope.getLesson(), filter.lessonId());
            return List.of(scope.getLesson());
        }
        if (filter.lessonId() != null) {
            var lesson = lessonRepository.findById(filter.lessonId())
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", filter.lessonId()));
            assertSameSubject(lesson, permission);
            return List.of(lesson);
        }
        return lessonRepository.findAll(LessonSpecifications.forPermission(permission));
    }

    private void assertSameSubject(Lesson lesson, TeacherSubjectPermission permission) {
        if (!lesson.getSubject().getId().equals(permission.getSubject().getId())) {
            throw new IllegalArgumentException(
                "Lesson " + lesson.getId() + " does not belong to subject of permission " + permission.getId());
        }
    }

    private void assertLessonMatch(Lesson scopeLesson, UUID requestedLessonId) {
        if (requestedLessonId != null && !scopeLesson.getId().equals(requestedLessonId)) {
            throw new IllegalArgumentException(
                "lessonScopeId belongs to lesson " + scopeLesson.getId() + " but lessonId=" + requestedLessonId);
        }
    }

    private GradingTableResponse buildTable(
        List<GradingAudienceScope> audience,
        List<Student> students,
        java.util.Map<Lesson, List<LessonScope>> visibleScopesByLesson
    ) {
        var studentIds = students.stream().map(Student::getId).toList();
        var lessonIds = visibleScopesByLesson.keySet().stream().map(Lesson::getId).toList();

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
            visibleScopesByLesson.entrySet()
                .stream()
                .map(e -> toGradingLesson(e.getKey(), e.getValue()))
                .toList(),
            assignments.stream().map(gradingMapper::toAssignmentResponse).toList(),
            grades.stream().map(gradingMapper::toCell).toList()
        );
    }

    private GradingTableLesson toGradingLesson(Lesson lesson, List<LessonScope> visibleScopes) {
        var scopes = visibleScopes.stream()
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
    public List<GradeCellResponse> upsertGrades(BulkUpsertGradesRequest request) {
        var items = request.items();

        var seenKeys = new HashSet<String>();
        for (var item : items) {
            var key = item.studentId() + "|" + item.lessonId() + "|" + item.assignmentId();
            if (!seenKeys.add(key)) {
                throw new IllegalArgumentException(
                    "Duplicate (studentId, lessonId, assignmentId) in request: " + item.studentId() + ", " + item.lessonId() + ", " + item.assignmentId());
            }
        }

        var assignmentIds = items.stream()
            .map(UpsertGradeRequest::assignmentId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        var assignmentsById = new HashMap<UUID, Assignment>();
        if (!assignmentIds.isEmpty()) {
            for (var a : assignmentRepository.findAllById(assignmentIds)) {
                assignmentsById.put(a.getId(), a);
            }
            for (var id : assignmentIds) {
                if (!assignmentsById.containsKey(id)) {
                    throw new ResourceNotFoundException("Assignment", id);
                }
            }
        }
        for (var item : items) {
            if (item.assignmentId() == null) {
                continue;
            }
            var a = assignmentsById.get(item.assignmentId());
            if (!a.getLesson().getId().equals(item.lessonId())) {
                throw new IllegalArgumentException(
                    "Assignment " + a.getId() + " does not belong to lesson " + item.lessonId());
            }
            if (item.score() > a.getMaxPoints()) {
                throw new IllegalArgumentException(
                    "Score " + item.score() + " exceeds assignment maxPoints " + a.getMaxPoints() + " for assignment " + a.getId());
            }
        }

        var studentIds = items.stream().map(UpsertGradeRequest::studentId).distinct().toList();
        var lessonIds = items.stream().map(UpsertGradeRequest::lessonId).distinct().toList();
        var existing = gradeRepository.findByLessonIdInAndStudentIdIn(lessonIds, studentIds);
        var existingByKey = new HashMap<String, Grade>();
        for (var g : existing) {
            var aId = g.getAssignment() != null
                      ? g.getAssignment().getId()
                      : null;
            existingByKey.put(g.getStudent().getId() + "|" + g.getLesson().getId() + "|" + aId, g);
        }

        var toSave = new ArrayList<Grade>(items.size());
        for (var item : items) {
            var key = item.studentId() + "|" + item.lessonId() + "|" + item.assignmentId();
            var assignmentRef = item.assignmentId() != null
                                ? assignmentsById.get(item.assignmentId())
                                : null;
            var grade = existingByKey.computeIfAbsent(key, k -> Grade.builder()
                .student(studentRepository.getReferenceById(item.studentId()))
                .lesson(lessonRepository.getReferenceById(item.lessonId()))
                .assignment(assignmentRef)
                .build());
            grade.setScore(item.score());
            grade.setComment(item.comment());
            toSave.add(grade);
        }

        var persisted = gradeRepository.saveAll(toSave);
        return persisted.stream().map(gradingMapper::toCell).toList();
    }

    @Override
    public List<AssignmentResponse> getAssignmentsByLesson(UUID lessonId) {
        return assignmentRepository.findByLessonIdInOrderByLessonIdAscOrderAsc(List.of(lessonId))
            .stream()
            .map(gradingMapper::toAssignmentResponse)
            .toList();
    }

    @Override
    public Map<UUID, List<AssignmentResponse>> getAssignmentsByLessons(java.util.Collection<UUID> lessonIds) {
        if (lessonIds.isEmpty()) {
            return Map.of();
        }
        var grouped = new HashMap<UUID, List<AssignmentResponse>>();
        for (var lessonId : lessonIds) {
            grouped.put(lessonId, new ArrayList<>());
        }
        for (var assignment : assignmentRepository.findByLessonIdInOrderByLessonIdAscOrderAsc(lessonIds)) {
            grouped.get(assignment.getLesson().getId()).add(gradingMapper.toAssignmentResponse(assignment));
        }
        return grouped;
    }

    @Transactional
    @Override
    public List<AssignmentResponse> createAssignments(CreateAssignmentsRequest request) {
        if (assignmentRepository.existsByLessonId(request.lessonId())) {
            throw new IllegalStateException("Lesson " + request.lessonId() + " already has assignments. " + "Use PUT /api/lessons/{id} to update individual assignments.");
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
    public List<AssignmentResponse> updateAssignmentsOfLesson(UUID lessonId, BulkUpdateAssignmentsRequest request) {
        var items = request.items();

        var seenIds = new HashSet<UUID>();
        for (var item : items) {
            if (!seenIds.add(item.id())) {
                throw new IllegalArgumentException("Duplicate assignment id in request: " + item.id());
            }
        }

        var assignments = assignmentRepository.findAllById(seenIds);
        if (assignments.size() != seenIds.size()) {
            var found = assignments.stream().map(Assignment::getId).collect(java.util.stream.Collectors.toSet());
            for (var id : seenIds) {
                if (!found.contains(id)) {
                    throw new ResourceNotFoundException("Assignment", id);
                }
            }
        }
        for (var a : assignments) {
            if (!a.getLesson().getId().equals(lessonId)) {
                throw new IllegalArgumentException(
                    "Assignment " + a.getId() + " does not belong to lesson " + lessonId);
            }
        }

        var assignmentsById = new HashMap<UUID, Assignment>();
        for (var a : assignments) {
            assignmentsById.put(a.getId(), a);
        }
        var itemsById = new HashMap<UUID, BulkUpdateAssignmentsRequest.Item>();
        for (var item : items) {
            itemsById.put(item.id(), item);
        }

        var siblings = assignmentRepository.findByLessonIdInOrderByLessonIdAscOrderAsc(List.of(lessonId));
        var ordersInLesson = new HashSet<Integer>();
        for (var sibling : siblings) {
            var override = itemsById.get(sibling.getId());
            int finalOrder = override != null
                             ? override.order()
                             : sibling.getOrder();
            if (!ordersInLesson.add(finalOrder)) {
                throw new IllegalArgumentException(
                    "Duplicate order " + finalOrder + " in lesson " + lessonId);
            }
        }

        for (var assignment : assignments) {
            assignment.setOrder(assignment.getOrder() + 1_000_000);
        }
        assignmentRepository.saveAllAndFlush(assignments);

        for (var item : items) {
            var assignment = assignmentsById.get(item.id());
            assignment.setOrder(item.order());
            assignment.setMaxPoints(item.maxPoints());
            assignment.setRequired(item.required());
        }
        var persisted = assignmentRepository.saveAll(assignments);
        var byIdPersisted = new HashMap<UUID, Assignment>();
        for (var a : persisted) {
            byIdPersisted.put(a.getId(), a);
        }
        return items.stream()
            .map(item -> gradingMapper.toAssignmentResponse(byIdPersisted.get(item.id())))
            .toList();
    }

    @Transactional
    @Override
    public void deleteAssignment(UUID id) {
        if (!assignmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Assignment", id);
        }
        assignmentRepository.deleteById(id);
    }

    @Transactional
    @Override
    public void deleteAssignmentsOfLesson(UUID lessonId) {
        assignmentRepository.deleteByLessonId(lessonId);
    }

    private List<Student> unionStudentsAcrossScopes(
        java.util.Collection<List<LessonScope>> scopesPerLesson
    ) {
        var seen = new LinkedHashMap<UUID, Student>();
        for (var scopes : scopesPerLesson) {
            for (var scope : scopes) {
                for (var s : lessonStudentsApi.studentsOf(scope)) {
                    seen.putIfAbsent(s.getId(), s);
                }
            }
        }
        var result = new ArrayList<>(seen.values());
        result.sort(Comparator.comparing(Student::getUsername));
        return result;
    }

    private List<GradingAudienceScope> audienceOf(TeacherSubjectPermission permission) {
        if (LessonSpecifications.permissionAllowsAllGroups(permission)) {
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
