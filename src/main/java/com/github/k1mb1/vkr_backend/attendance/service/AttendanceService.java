package com.github.k1mb1.vkr_backend.attendance.service;

import com.github.k1mb1.vkr_backend.attendance.AttendanceStatus;
import com.github.k1mb1.vkr_backend.attendance.api.AttendanceApi;
import com.github.k1mb1.vkr_backend.attendance.api.AttendanceAudienceScope;
import com.github.k1mb1.vkr_backend.attendance.api.AttendanceCellResponse;
import com.github.k1mb1.vkr_backend.attendance.api.AttendanceSummaryResponse;
import com.github.k1mb1.vkr_backend.attendance.api.AttendanceTableResponse;
import com.github.k1mb1.vkr_backend.attendance.domain.AttendanceEntity;
import com.github.k1mb1.vkr_backend.attendance.mapper.AttendanceMapper;
import com.github.k1mb1.vkr_backend.attendance.repository.AttendanceLessonRepository;
import com.github.k1mb1.vkr_backend.attendance.repository.AttendanceLessonScopeRepository;
import com.github.k1mb1.vkr_backend.attendance.repository.AttendancePermissionRepository;
import com.github.k1mb1.vkr_backend.attendance.repository.AttendanceRepository;
import com.github.k1mb1.vkr_backend.attendance.repository.AttendanceStudentRefRepository;
import com.github.k1mb1.vkr_backend.attendance.service.dto.filter.AttendanceFilter;
import com.github.k1mb1.vkr_backend.attendance.service.dto.request.BulkUpsertAttendanceRequest;
import com.github.k1mb1.vkr_backend.attendance.service.dto.request.UpsertAttendanceRequest;
import com.github.k1mb1.vkr_backend.common.exception.ResourceNotFoundException;
import com.github.k1mb1.vkr_backend.lesson.api.LessonStudentResponse;
import com.github.k1mb1.vkr_backend.lesson.api.LessonStudentsApi;
import com.github.k1mb1.vkr_backend.lesson.domain.LessonEntity;
import com.github.k1mb1.vkr_backend.lesson.domain.LessonScopeEntity;
import com.github.k1mb1.vkr_backend.lesson.specification.LessonSpecifications;
import com.github.k1mb1.vkr_backend.subject.api.AttendanceHighlightPolicyResponse;
import com.github.k1mb1.vkr_backend.subject.domain.TeacherSubjectPermissionEntity;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceService implements AttendanceApi {

    final AttendanceRepository attendanceRepository;

    final AttendanceMapper attendanceMapper;

    final AttendancePermissionRepository permissionRepository;

    final AttendanceLessonRepository lessonRepository;

    final AttendanceLessonScopeRepository lessonScopeRepository;

    final AttendanceStudentRefRepository studentRefRepository;

    final LessonStudentsApi lessonStudentsApi;

    @Override
    @PreAuthorize("@authz.ownsPermission(#permissionId)")
    public AttendanceTableResponse getAttendanceTable(
            UUID permissionId, @Nullable UUID lessonScopeId, @Nullable UUID lessonId) {
        return getAttendanceTable(AttendanceFilter.builder()
                .permissionId(permissionId)
                .lessonScopeId(lessonScopeId)
                .lessonId(lessonId)
                .build());
    }

    @PreAuthorize("@authz.ownsPermission(#filter.permissionId())")
    public AttendanceTableResponse getAttendanceTable(AttendanceFilter filter) {
        var permission = permissionRepository
                .findWithDetailsById(filter.permissionId())
                .orElseThrow(() -> new ResourceNotFoundException("TeacherSubjectPermission", filter.permissionId()));

        var lessons = resolveLessons(permission, filter.lessonScopeId(), filter.lessonId());
        var scopes = resolveScopes(lessons, permission, filter);

        var students = lessonStudentsApi.studentsOfScopes(
                scopes.stream().map(LessonScopeEntity::getId).toList());
        var audience = audienceOf(permission);
        var highlightPolicy = attendanceMapper.toHighlightPolicyResponse(
                permission.getSubject().getAttendanceHighlightPolicy());

        return buildTable(highlightPolicy, audience, students, scopes);
    }

    private List<LessonScopeEntity> resolveScopes(
            List<LessonEntity> lessons, TeacherSubjectPermissionEntity permission, AttendanceFilter filter) {
        var visible = visibleScopesIn(lessons, permission);
        if (filter.lessonScopeId() == null) {
            return visible;
        }
        var narrowed = visible.stream()
                .filter(s -> s.getId().equals(filter.lessonScopeId()))
                .toList();
        if (narrowed.isEmpty()) {
            throw new IllegalArgumentException(
                    "Scope " + filter.lessonScopeId() + " is not visible under permission " + permission.getId());
        }
        return narrowed;
    }

    private AttendanceTableResponse buildTable(
            AttendanceHighlightPolicyResponse highlightPolicy,
            List<AttendanceAudienceScope> audience,
            List<LessonStudentResponse> students,
            List<LessonScopeEntity> scopes) {
        var studentIds = students.stream().map(LessonStudentResponse::id).toList();
        var scopeIds = scopes.stream().map(LessonScopeEntity::getId).toList();

        var attendances = studentIds.isEmpty() || scopeIds.isEmpty()
                ? List.<AttendanceEntity>of()
                : attendanceRepository.findByLessonScopeIdInAndStudentIdIn(scopeIds, studentIds);

        return AttendanceTableResponse.builder()
                .highlightPolicy(highlightPolicy)
                .audience(audience)
                .students(
                        students.stream().map(attendanceMapper::toTableStudent).toList())
                .lessons(scopes.stream().map(attendanceMapper::toTableLesson).toList())
                .attendances(attendances.stream().map(attendanceMapper::toCell).toList())
                .build();
    }

    @Transactional
    @PreAuthorize("@authz.canAccessLessonScopes(#request.items().![lessonScopeId()])")
    public List<AttendanceCellResponse> upsertAll(BulkUpsertAttendanceRequest request) {
        var items = request.items();
        var seen = new HashSet<String>();
        for (var item : items) {
            var key = item.studentId() + "|" + item.lessonScopeId();
            if (!seen.add(key)) {
                throw new IllegalArgumentException(
                        "Duplicate (studentId, lessonScopeId) in request: " + item.studentId()
                                + ", "
                                + item.lessonScopeId());
            }
        }

        var studentIds = items.stream()
                .map(UpsertAttendanceRequest::studentId)
                .distinct()
                .toList();
        var scopeIds = items.stream()
                .map(UpsertAttendanceRequest::lessonScopeId)
                .distinct()
                .toList();
        var existing = attendanceRepository.findByLessonScopeIdInAndStudentIdIn(scopeIds, studentIds);
        var existingByKey = new HashMap<String, AttendanceEntity>();
        for (var a : existing) {
            existingByKey.put(a.getStudent().getId() + "|" + a.getLessonScope().getId(), a);
        }

        var saved = new ArrayList<AttendanceEntity>(items.size());
        for (var item : items) {
            var key = item.studentId() + "|" + item.lessonScopeId();
            var attendance = existingByKey.computeIfAbsent(
                    key,
                    k -> AttendanceEntity.builder()
                            .student(studentRefRepository.getReferenceById(item.studentId()))
                            .lessonScope(lessonScopeRepository.getReferenceById(item.lessonScopeId()))
                            .build());
            attendance.setStatus(item.status());
            attendance.setComment(item.comment());
            saved.add(attendance);
        }

        var persisted = attendanceRepository.saveAll(saved);
        return persisted.stream().map(attendanceMapper::toCell).toList();
    }

    @Override
    public Map<UUID, AttendanceSummaryResponse> summarize(
            Collection<UUID> lessonScopeIds, Collection<UUID> studentIds) {
        if (lessonScopeIds.isEmpty() || studentIds.isEmpty()) {
            return Map.of();
        }
        var rows = attendanceRepository.findByLessonScopeIdInAndStudentIdIn(lessonScopeIds, studentIds);
        // Счётчик по каждому статусу посещаемости на студента (индексируется ordinal статуса).
        var counts = new HashMap<UUID, int[]>();
        for (var a : rows) {
            var c = counts.computeIfAbsent(a.getStudent().getId(), k -> new int[AttendanceStatus.values().length]);
            c[a.getStatus().ordinal()]++;
        }
        var result = new HashMap<UUID, AttendanceSummaryResponse>();
        counts.forEach((id, c) -> result.put(
                id,
                AttendanceSummaryResponse.builder()
                        .present(c[AttendanceStatus.PRESENT.ordinal()])
                        .late(c[AttendanceStatus.LATE.ordinal()])
                        .absent(c[AttendanceStatus.ABSENT.ordinal()])
                        .excused(c[AttendanceStatus.EXCUSED.ordinal()])
                        .build()));
        return result;
    }

    /** Разрешение занятий по фильтру (scope/lesson/все видимые) — см. одноимённую логику модуля lesson. */
    private List<LessonEntity> resolveLessons(
            TeacherSubjectPermissionEntity permission, @Nullable UUID lessonScopeId, @Nullable UUID lessonId) {
        if (lessonScopeId != null) {
            var scope = lessonScopeRepository
                    .findWithDetailsById(lessonScopeId)
                    .orElseThrow(() -> new ResourceNotFoundException("LessonScope", lessonScopeId));
            LessonSpecifications.assertSameSubject(scope.getLesson(), permission);
            LessonSpecifications.assertLessonMatch(scope.getLesson(), lessonId);
            return List.of(scope.getLesson());
        }
        if (lessonId != null) {
            var lesson = lessonRepository
                    .findById(lessonId)
                    .orElseThrow(() -> new ResourceNotFoundException("Lesson", lessonId));
            LessonSpecifications.assertSameSubject(lesson, permission);
            return List.of(lesson);
        }
        return lessonRepository.findAll(LessonSpecifications.forPermission(permission));
    }

    private List<LessonScopeEntity> visibleScopesIn(
            List<LessonEntity> lessons, TeacherSubjectPermissionEntity permission) {
        var result = new ArrayList<LessonScopeEntity>();
        for (var lesson : lessons) {
            result.addAll(LessonSpecifications.visibleScopes(lesson, permission));
        }
        result.sort(Comparator.comparing(
                        (LessonScopeEntity s) -> s.getStartedAt(), Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(s -> s.getLesson().getOrderIndex()));
        return result;
    }

    private List<AttendanceAudienceScope> audienceOf(TeacherSubjectPermissionEntity permission) {
        return LessonSpecifications.audienceScopes(permission).stream()
                .map(s -> {
                    // audienceScopes() excludes all-groups scopes, so the group is always present.
                    var group = Objects.requireNonNull(s.getGroup());
                    return AttendanceAudienceScope.builder()
                            .groupId(group.getId())
                            .groupName(group.getName())
                            .allowedSubgroupId(
                                    s.getAllowedSubgroup() != null
                                            ? s.getAllowedSubgroup().getId()
                                            : null)
                            .allowedSubgroupIndex(
                                    s.getAllowedSubgroup() != null
                                            ? s.getAllowedSubgroup().getIndex()
                                            : null)
                            .build();
                })
                .toList();
    }
}
