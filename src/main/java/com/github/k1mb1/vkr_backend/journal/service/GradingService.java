package com.github.k1mb1.vkr_backend.journal.service;

import com.github.k1mb1.vkr_backend.auth.AuthorizationService;
import com.github.k1mb1.vkr_backend.common.exception.ConflictException;
import com.github.k1mb1.vkr_backend.common.exception.ResourceNotFoundException;
import com.github.k1mb1.vkr_backend.journal.AssignmentAdmissionMode;
import com.github.k1mb1.vkr_backend.journal.domain.AssignmentEntity;
import com.github.k1mb1.vkr_backend.journal.domain.GradeEntity;
import com.github.k1mb1.vkr_backend.journal.mapper.GradingMapper;
import com.github.k1mb1.vkr_backend.journal.repository.AssignmentRepository;
import com.github.k1mb1.vkr_backend.journal.repository.GradeRepository;
import com.github.k1mb1.vkr_backend.journal.repository.JournalLessonRepository;
import com.github.k1mb1.vkr_backend.journal.repository.JournalLessonScopeRepository;
import com.github.k1mb1.vkr_backend.journal.repository.JournalPermissionRepository;
import com.github.k1mb1.vkr_backend.journal.repository.JournalStudentRefRepository;
import com.github.k1mb1.vkr_backend.journal.service.dto.filter.GradingFilter;
import com.github.k1mb1.vkr_backend.journal.service.dto.request.BulkUpdateAssignmentsRequest;
import com.github.k1mb1.vkr_backend.journal.service.dto.request.BulkUpsertGradesRequest;
import com.github.k1mb1.vkr_backend.journal.service.dto.request.CreateAssignmentsRequest;
import com.github.k1mb1.vkr_backend.journal.service.dto.request.UpsertGradeRequest;
import com.github.k1mb1.vkr_backend.journal.service.dto.response.AssignmentResponse;
import com.github.k1mb1.vkr_backend.journal.service.dto.response.AttendanceSummaryResponse;
import com.github.k1mb1.vkr_backend.journal.service.dto.response.GradeCellResponse;
import com.github.k1mb1.vkr_backend.journal.service.dto.response.GradingTableLessonResponse;
import com.github.k1mb1.vkr_backend.journal.service.dto.response.GradingTableResponse;
import com.github.k1mb1.vkr_backend.journal.service.dto.response.JournalAudienceScopeResponse;
import com.github.k1mb1.vkr_backend.journal.service.dto.response.StudentAttendanceResponse;
import com.github.k1mb1.vkr_backend.lesson.api.LessonStudentResponse;
import com.github.k1mb1.vkr_backend.lesson.api.LessonStudentsApi;
import com.github.k1mb1.vkr_backend.lesson.domain.LessonEntity;
import com.github.k1mb1.vkr_backend.lesson.domain.LessonScopeEntity;
import com.github.k1mb1.vkr_backend.lesson.specification.LessonSpecifications;
import com.github.k1mb1.vkr_backend.subject.api.AttendancePolicyResponse;
import com.github.k1mb1.vkr_backend.subject.api.FinalAssessmentPolicyResponse;
import com.github.k1mb1.vkr_backend.subject.api.GradingHighlightPolicyResponse;
import com.github.k1mb1.vkr_backend.subject.api.PenaltyPolicyResponse;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GradingService {

    // Временный сдвиг order при переупорядочивании заданий, чтобы обойти уникальное
    // ограничение (lesson_id, order) до применения финальных значений.
    private static final int ORDER_RESHUFFLE_OFFSET = 1_000_000;

    final GradeRepository gradeRepository;

    final AssignmentRepository assignmentRepository;

    final GradingMapper gradingMapper;

    final JournalPermissionRepository permissionRepository;

    final JournalLessonRepository lessonRepository;

    final JournalLessonScopeRepository lessonScopeRepository;

    final JournalStudentRefRepository studentRefRepository;

    final AttendanceService attendanceService;

    final LessonStudentsApi lessonStudentsApi;

    final AuthorizationService authz;

    final JournalAudienceService audienceService;

    static @Nullable LocalDate earliestStartedAt(LessonEntity lesson) {
        return lesson.getScopes().stream()
                .map(LessonScopeEntity::getStartedAt)
                .filter(Objects::nonNull)
                .min(Comparator.naturalOrder())
                .orElse(null);
    }

    /**
     * «Ранг» занятия среди занятий с заданиями: количество занятий с заданиями,
     * имеющих orderIndex {@code <=} данного. Используется для вычисления
     * {@code lessonsOffset} через разность рангов (только занятия с заданиями учитываются).
     */
    private static int assignmentRank(LessonEntity lesson, List<LessonEntity> orderedLessonsWithAssignments) {
        int idx = Collections.binarySearch(
                orderedLessonsWithAssignments, lesson, Comparator.comparingInt(LessonEntity::getOrderIndex));
        if (idx >= 0) {
            return idx + 1; // 1-based: сколько занятий с заданиями <= данного
        }
        return -idx - 1; // insertion point = сколько занятий с заданиями < данного (== <=, т.к. нет в списке)
    }

    @PreAuthorize("@authz.ownsPermission(#permissionId)")
    public GradingTableResponse getGradingTable(
            UUID permissionId, @Nullable UUID lessonScopeId, @Nullable UUID lessonId) {
        return getGradingTable(GradingFilter.builder()
                .permissionId(permissionId)
                .lessonScopeId(lessonScopeId)
                .lessonId(lessonId)
                .build());
    }

    @PreAuthorize("@authz.ownsPermission(#filter.permissionId())")
    public GradingTableResponse getGradingTable(GradingFilter filter) {
        var permission = permissionRepository
                .findWithDetailsById(filter.permissionId())
                .orElseThrow(() -> new ResourceNotFoundException("TeacherSubjectPermission", filter.permissionId()));

        var lessons =
                lessonRepository
                        .resolveLessons(lessonScopeRepository, permission, filter.lessonScopeId(), filter.lessonId())
                        .stream()
                        .sorted(Comparator.comparing(
                                        GradingService::earliestStartedAt,
                                        Comparator.nullsLast(Comparator.naturalOrder()))
                                .thenComparingInt(LessonEntity::getOrderIndex))
                        .toList();

        var visibleScopesByLesson = new java.util.LinkedHashMap<LessonEntity, List<LessonScopeEntity>>();
        for (var lesson : lessons) {
            visibleScopesByLesson.put(lesson, LessonSpecifications.visibleScopes(lesson, permission));
        }

        var students = lessonStudentsApi.studentsOfScopes(visibleScopesByLesson.values().stream()
                .flatMap(List::stream)
                .map(LessonScopeEntity::getId)
                .toList());
        var audience = audienceService.audienceOf(permission);
        var penaltyPolicy =
                gradingMapper.toPenaltyPolicyResponse(permission.getSubject().getPenaltyPolicy());
        var attendancePolicy =
                gradingMapper.toAttendancePolicyResponse(permission.getSubject().getAttendancePolicy());
        var highlightPolicy = gradingMapper.toGradingHighlightPolicyResponse(
                permission.getSubject().getGradingHighlightPolicy());
        var finalAssessmentPolicy = gradingMapper.toFinalAssessmentPolicyResponse(
                permission.getSubject().getFinalAssessmentPolicy());

        return buildTable(
                penaltyPolicy,
                attendancePolicy,
                highlightPolicy,
                finalAssessmentPolicy,
                audience,
                students,
                visibleScopesByLesson);
    }

    private GradingTableResponse buildTable(
            PenaltyPolicyResponse penaltyPolicy,
            AttendancePolicyResponse attendancePolicy,
            GradingHighlightPolicyResponse highlightPolicy,
            FinalAssessmentPolicyResponse finalAssessmentPolicy,
            List<JournalAudienceScopeResponse> audience,
            List<LessonStudentResponse> students,
            java.util.Map<LessonEntity, List<LessonScopeEntity>> visibleScopesByLesson) {
        var studentIds = students.stream().map(LessonStudentResponse::id).toList();
        var lessonIds =
                visibleScopesByLesson.keySet().stream().map(LessonEntity::getId).toList();
        var scopeIds = visibleScopesByLesson.values().stream()
                .flatMap(List::stream)
                .map(LessonScopeEntity::getId)
                .toList();

        var assignments = lessonIds.isEmpty()
                ? List.<AssignmentEntity>of()
                : assignmentRepository.findByLessonIdInOrderByLessonIdAscOrderAsc(lessonIds);

        var grades = studentIds.isEmpty() || lessonIds.isEmpty()
                ? List.<GradeEntity>of()
                : gradeRepository.findByLessonIdInAndStudentIdIn(lessonIds, studentIds);

        var attendanceByStudent = attendanceService.summarize(scopeIds, studentIds);
        var attendance = students.stream()
                .map(s -> {
                    var sum = attendanceByStudent.getOrDefault(
                            s.id(),
                            AttendanceSummaryResponse.builder()
                                    .present(0)
                                    .late(0)
                                    .absent(0)
                                    .excused(0)
                                    .build());
                    return StudentAttendanceResponse.builder()
                            .studentId(s.id())
                            .present(sum.present())
                            .late(sum.late())
                            .absent(sum.absent())
                            .excused(sum.excused())
                            .build();
                })
                .toList();

        return GradingTableResponse.builder()
                .penaltyPolicy(penaltyPolicy)
                .attendancePolicy(attendancePolicy)
                .highlightPolicy(highlightPolicy)
                .finalAssessmentPolicy(finalAssessmentPolicy)
                .attendance(attendance)
                .audience(audience)
                .students(students.stream().map(gradingMapper::toTableStudent).toList())
                .lessons(visibleScopesByLesson.entrySet().stream()
                        .map(e -> toGradingLesson(e.getKey(), e.getValue()))
                        .toList())
                .assignments(assignments.stream()
                        .map(gradingMapper::toAssignmentResponse)
                        .toList())
                .grades(grades.stream().map(gradingMapper::toCell).toList())
                .build();
    }

    private GradingTableLessonResponse toGradingLesson(LessonEntity lesson, List<LessonScopeEntity> visibleScopes) {
        var scopes = visibleScopes.stream()
                .sorted(Comparator.comparing(
                        (LessonScopeEntity s) -> s.getStartedAt(), Comparator.nullsLast(Comparator.naturalOrder())))
                .map(s -> new GradingTableLessonResponse.Scope(
                        s.getId(),
                        s.getGroup() != null ? s.getGroup().getId() : null,
                        s.getAllowedSubgroup() != null ? s.getAllowedSubgroup().getId() : null,
                        s.getStartedAt(),
                        s.isAllGroups()))
                .toList();
        return new GradingTableLessonResponse(
                lesson.getId(), lesson.getType(), lesson.getOrderIndex(), lesson.getTopic(), lesson.isActive(), scopes);
    }

    @Transactional
    @PreAuthorize("@authz.canAccessLessons(#request.items().![lessonId()])")
    public List<GradeCellResponse> upsertGrades(BulkUpsertGradesRequest request) {
        var items = request.items();

        var seenKeys = new HashSet<String>();
        for (var item : items) {
            var key = item.studentId() + "|" + item.lessonId() + "|" + item.assignmentId();
            if (!seenKeys.add(key)) {
                throw new IllegalArgumentException(
                        "Duplicate (studentId, lessonId, assignmentId) in request: " + item.studentId()
                                + ", "
                                + item.lessonId()
                                + ", "
                                + item.assignmentId());
            }
        }

        var assignmentIds = items.stream()
                .map(UpsertGradeRequest::assignmentId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        var assignmentsById = new HashMap<UUID, AssignmentEntity>();
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
            var a = Objects.requireNonNull(assignmentsById.get(item.assignmentId()));
            if (!a.getLesson().getId().equals(item.lessonId())) {
                throw new IllegalArgumentException(
                        "Assignment " + a.getId() + " does not belong to lesson " + item.lessonId());
            }
            if (item.score() > a.getMaxPoints()) {
                throw new IllegalArgumentException("Score " + item.score()
                        + " exceeds assignment maxPoints "
                        + a.getMaxPoints()
                        + " for assignment "
                        + a.getId());
            }
        }

        var studentIds =
                items.stream().map(UpsertGradeRequest::studentId).distinct().toList();
        var lessonIds =
                items.stream().map(UpsertGradeRequest::lessonId).distinct().toList();
        var existing = gradeRepository.findByLessonIdInAndStudentIdIn(lessonIds, studentIds);
        var existingByKey = new HashMap<String, GradeEntity>();
        for (var g : existing) {
            var aId = g.getAssignment() != null ? g.getAssignment().getId() : null;
            existingByKey.put(g.getStudent().getId() + "|" + g.getLesson().getId() + "|" + aId, g);
        }

        // Кэш активного занятия и списка занятий с заданиями по (subjectId|type).
        var activeCache = new HashMap<String, Optional<LessonEntity>>();
        var lessonsWithAssignmentsCache = new HashMap<String, List<LessonEntity>>();

        var toSave = new ArrayList<GradeEntity>(items.size());
        for (var item : items) {
            var key = item.studentId() + "|" + item.lessonId() + "|" + item.assignmentId();
            var assignmentRef = item.assignmentId() != null ? assignmentsById.get(item.assignmentId()) : null;

            // Смещение сдачи фиксируем один раз при создании ячейки и только для оценок с заданием.
            // Считается только по занятиям, у которых есть задания:
            // разность «рангов» активного и заданного занятий среди занятий этого типа с заданиями.
            LessonEntity awarded = null;
            Integer lessonsOffset = null;
            if (assignmentRef != null) {
                var dueLesson = assignmentRef.getLesson();
                var cacheKey = dueLesson.getSubject().getId() + "|" + dueLesson.getType();
                var active = activeCache.computeIfAbsent(
                        cacheKey,
                        k -> lessonRepository.findBySubjectIdAndTypeAndActiveTrue(
                                dueLesson.getSubject().getId(), dueLesson.getType()));
                if (active.isPresent()) {
                    awarded = active.get();
                    var lessonsWithAssignments = lessonsWithAssignmentsCache.computeIfAbsent(
                            cacheKey,
                            k -> lessonRepository.findWithAssignmentsBySubjectIdAndType(
                                    dueLesson.getSubject().getId(), dueLesson.getType()));
                    lessonsOffset = assignmentRank(awarded, lessonsWithAssignments)
                            - assignmentRank(dueLesson, lessonsWithAssignments);
                }
            }
            final var awardedLesson = awarded;
            final var offset = lessonsOffset;

            var grade = existingByKey.computeIfAbsent(
                    key,
                    k -> GradeEntity.builder()
                            .student(studentRefRepository.getReferenceById(item.studentId()))
                            .lesson(lessonRepository.getReferenceById(item.lessonId()))
                            .assignment(assignmentRef)
                            .awardedLesson(awardedLesson)
                            .lessonsOffset(offset)
                            .build());
            grade.setScore(item.score());
            grade.setComment(item.comment());
            toSave.add(grade);
        }

        var persisted = gradeRepository.saveAll(toSave);
        return persisted.stream().map(gradingMapper::toCell).toList();
    }

    @PreAuthorize("@authz.canAccessLesson(#lessonId)")
    public List<AssignmentResponse> getAssignmentsByLesson(UUID lessonId) {
        return assignmentRepository.findByLessonIdInOrderByLessonIdAscOrderAsc(List.of(lessonId)).stream()
                .map(gradingMapper::toAssignmentResponse)
                .toList();
    }

    public Map<UUID, List<AssignmentResponse>> getAssignmentsByLessons(java.util.Collection<UUID> lessonIds) {
        if (lessonIds.isEmpty()) {
            return Map.of();
        }
        var grouped = new HashMap<UUID, List<AssignmentResponse>>();
        for (var lessonId : lessonIds) {
            grouped.put(lessonId, new ArrayList<>());
        }
        for (var assignment : assignmentRepository.findByLessonIdInOrderByLessonIdAscOrderAsc(lessonIds)) {
            Objects.requireNonNull(grouped.get(assignment.getLesson().getId()))
                    .add(gradingMapper.toAssignmentResponse(assignment));
        }
        return grouped;
    }

    @Transactional
    @PreAuthorize("@authz.canAccessLesson(#request.lessonId())")
    public List<AssignmentResponse> createAssignments(CreateAssignmentsRequest request) {
        if (assignmentRepository.existsByLessonId(request.lessonId())) {
            throw new ConflictException("Lesson " + request.lessonId()
                    + " already has assignments. "
                    + "Use PUT /api/lessons/{id} to update individual assignments.");
        }
        var lessonRef = lessonRepository.getReferenceById(request.lessonId());
        var items = request.items();
        for (var item : items) {
            validateAdmission(item.admissionMode(), item.admissionMinScore());
        }
        var assignments = new ArrayList<AssignmentEntity>(items.size());
        for (int i = 0; i < items.size(); i++) {
            var item = items.get(i);
            assignments.add(AssignmentEntity.builder()
                    .lesson(lessonRef)
                    .order(i + 1)
                    .maxPoints(item.maxPoints())
                    .required(item.required())
                    .admissionMode(item.admissionMode())
                    .admissionMinScore(item.admissionMinScore())
                    .admissionTiers(
                            item.admissionTiers() != null
                                    ? item.admissionTiers().stream()
                                            .map(
                                                    t -> com.github.k1mb1.vkr_backend.journal.domain
                                                            .AssignmentAdmissionTier.builder()
                                                            .bandId(t.bandId())
                                                            .minScore(t.minScore())
                                                            .build())
                                            .toList()
                                    : new ArrayList<>())
                    .build());
        }
        return assignmentRepository.saveAll(assignments).stream()
                .map(gradingMapper::toAssignmentResponse)
                .toList();
    }

    @Transactional
    @PreAuthorize("@authz.canAccessLesson(#lessonId)")
    public List<AssignmentResponse> updateAssignmentsOfLesson(UUID lessonId, BulkUpdateAssignmentsRequest request) {
        var items = request.items();
        for (var item : items) {
            validateAdmission(item.admissionMode(), item.admissionMinScore());
        }

        var seenIds = new HashSet<UUID>();
        for (var item : items) {
            if (!seenIds.add(item.id())) {
                throw new IllegalArgumentException("Duplicate assignment id in request: " + item.id());
            }
        }

        var assignments = assignmentRepository.findAllById(seenIds);
        if (assignments.size() != seenIds.size()) {
            var found = assignments.stream().map(AssignmentEntity::getId).collect(java.util.stream.Collectors.toSet());
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

        var assignmentsById = new HashMap<UUID, AssignmentEntity>();
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
            int finalOrder = override != null ? override.order() : sibling.getOrder();
            if (!ordersInLesson.add(finalOrder)) {
                throw new IllegalArgumentException("Duplicate order " + finalOrder + " in lesson " + lessonId);
            }
        }

        for (var assignment : assignments) {
            assignment.setOrder(assignment.getOrder() + ORDER_RESHUFFLE_OFFSET);
        }
        assignmentRepository.saveAllAndFlush(assignments);

        for (var item : items) {
            var assignment = Objects.requireNonNull(assignmentsById.get(item.id()));
            assignment.setOrder(item.order());
            assignment.setMaxPoints(item.maxPoints());
            assignment.setRequired(item.required());
            assignment.setAdmissionMode(item.admissionMode());
            assignment.setAdmissionMinScore(item.admissionMinScore());
            assignment.setAdmissionTiers(
                    item.admissionTiers() != null
                            ? item.admissionTiers().stream()
                                    .map(t ->
                                            com.github.k1mb1.vkr_backend.journal.domain.AssignmentAdmissionTier
                                                    .builder()
                                                    .bandId(t.bandId())
                                                    .minScore(t.minScore())
                                                    .build())
                                    .toList()
                            : new ArrayList<>());
        }
        var persisted = assignmentRepository.saveAll(assignments);
        var byIdPersisted = new HashMap<UUID, AssignmentEntity>();
        for (var a : persisted) {
            byIdPersisted.put(a.getId(), a);
        }
        return items.stream()
                .map(item -> gradingMapper.toAssignmentResponse(Objects.requireNonNull(byIdPersisted.get(item.id()))))
                .toList();
    }

    @Transactional
    public void deleteAssignment(UUID id) {
        // Доступ по id задания: резолвим предмет задания и проверяем доступ к нему.
        // (через @PreAuthorize нельзя — решение зависит от сущности, которую ещё надо загрузить).
        var subjectId = assignmentRepository
                .findSubjectIdById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment", id));
        if (!authz.canAccessSubject(subjectId)) {
            throw new AccessDeniedException("No access to assignment " + id);
        }
        assignmentRepository.deleteById(id);
    }

    @Transactional
    @PreAuthorize("@authz.canAccessLesson(#lessonId)")
    public void deleteAssignmentsOfLesson(UUID lessonId) {
        assignmentRepository.deleteByLessonId(lessonId);
    }

    private void validateAdmission(AssignmentAdmissionMode mode, Integer admissionMinScore) {
        if (mode == AssignmentAdmissionMode.PASS_FAIL && admissionMinScore != null) {
            throw new IllegalArgumentException("PASS_FAIL mode does not require admissionMinScore");
        }
    }
}
