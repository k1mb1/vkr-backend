package com.github.k1mb1.vkr_backend.attendance.service;

import com.github.k1mb1.vkr_backend.attendance.api.AttendanceApi;
import com.github.k1mb1.vkr_backend.attendance.api.AttendanceSummary;
import com.github.k1mb1.vkr_backend.attendance.domain.AttendanceEntity;
import com.github.k1mb1.vkr_backend.attendance.domain.AttendanceStatus;
import com.github.k1mb1.vkr_backend.attendance.mapper.AttendanceMapper;
import com.github.k1mb1.vkr_backend.attendance.repository.AttendanceRepository;
import com.github.k1mb1.vkr_backend.attendance.service.dto.filter.AttendanceFilter;
import com.github.k1mb1.vkr_backend.attendance.service.dto.request.BulkUpsertAttendanceRequest;
import com.github.k1mb1.vkr_backend.attendance.service.dto.request.UpsertAttendanceRequest;
import com.github.k1mb1.vkr_backend.attendance.service.dto.response.AttendanceAudienceScope;
import com.github.k1mb1.vkr_backend.attendance.service.dto.response.AttendanceCellResponse;
import com.github.k1mb1.vkr_backend.attendance.service.dto.response.AttendanceTableResponse;
import com.github.k1mb1.vkr_backend.common.exception.ResourceNotFoundException;
import com.github.k1mb1.vkr_backend.group.domain.StudentEntity;
import com.github.k1mb1.vkr_backend.group.repository.StudentRepository;
import com.github.k1mb1.vkr_backend.lesson.api.LessonStudentsApi;
import com.github.k1mb1.vkr_backend.lesson.domain.LessonEntity;
import com.github.k1mb1.vkr_backend.lesson.domain.LessonScopeEntity;
import com.github.k1mb1.vkr_backend.lesson.repository.LessonScopeRepository;
import com.github.k1mb1.vkr_backend.lesson.service.LessonResolver;
import com.github.k1mb1.vkr_backend.lesson.specification.LessonSpecifications;
import com.github.k1mb1.vkr_backend.subject.domain.TeacherSubjectPermissionEntity;
import com.github.k1mb1.vkr_backend.subject.mapper.SubjectMapper;
import com.github.k1mb1.vkr_backend.subject.repository.TeacherSubjectPermissionRepository;
import com.github.k1mb1.vkr_backend.subject.service.dto.response.AttendanceHighlightPolicyResponse;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceService implements AttendanceApi {

    final AttendanceRepository attendanceRepository;

    final AttendanceMapper attendanceMapper;

    final TeacherSubjectPermissionRepository permissionRepository;

    final LessonResolver lessonResolver;

    final LessonScopeRepository lessonScopeRepository;

    final StudentRepository studentRepository;

    final LessonStudentsApi lessonStudentsApi;

    final SubjectMapper subjectMapper;

    @Override
    @PreAuthorize("@authz.ownsPermission(#filter.permissionId())")
    public AttendanceTableResponse getAttendanceTable(AttendanceFilter filter) {
        var permission = permissionRepository
                .findWithDetailsById(filter.permissionId())
                .orElseThrow(() -> new ResourceNotFoundException("TeacherSubjectPermission", filter.permissionId()));

        var lessons = lessonResolver.resolveLessons(permission, filter.lessonScopeId(), filter.lessonId());
        var scopes = resolveScopes(lessons, permission, filter);

        var students = lessonStudentsApi.studentsOf(scopes);
        var audience = audienceOf(permission);
        var highlightPolicy = subjectMapper.toAttendanceHighlightPolicyResponse(
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
            List<StudentEntity> students,
            List<LessonScopeEntity> scopes) {
        var studentIds = students.stream().map(StudentEntity::getId).toList();
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
    @Override
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
                            .student(studentRepository.getReferenceById(item.studentId()))
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
    public Map<UUID, AttendanceSummary> summarize(Collection<UUID> lessonScopeIds, Collection<UUID> studentIds) {
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
        var result = new HashMap<UUID, AttendanceSummary>();
        counts.forEach((id, c) -> result.put(
                id,
                AttendanceSummary.builder()
                        .present(c[AttendanceStatus.PRESENT.ordinal()])
                        .late(c[AttendanceStatus.LATE.ordinal()])
                        .absent(c[AttendanceStatus.ABSENT.ordinal()])
                        .excused(c[AttendanceStatus.EXCUSED.ordinal()])
                        .build()));
        return result;
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
        return lessonResolver.audienceScopes(permission).stream()
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
