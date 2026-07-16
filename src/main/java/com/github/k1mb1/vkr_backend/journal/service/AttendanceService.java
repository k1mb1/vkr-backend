package com.github.k1mb1.vkr_backend.journal.service;

import com.github.k1mb1.vkr_backend.common.exception.ResourceNotFoundException;
import com.github.k1mb1.vkr_backend.journal.AttendanceStatus;
import com.github.k1mb1.vkr_backend.journal.domain.AttendanceEntity;
import com.github.k1mb1.vkr_backend.journal.mapper.AttendanceMapper;
import com.github.k1mb1.vkr_backend.journal.repository.AttendanceRepository;
import com.github.k1mb1.vkr_backend.journal.repository.JournalLessonRepository;
import com.github.k1mb1.vkr_backend.journal.repository.JournalLessonScopeRepository;
import com.github.k1mb1.vkr_backend.journal.repository.JournalPermissionRepository;
import com.github.k1mb1.vkr_backend.journal.repository.JournalStudentRefRepository;
import com.github.k1mb1.vkr_backend.journal.service.dto.filter.AttendanceFilter;
import com.github.k1mb1.vkr_backend.journal.service.dto.request.BulkUpsertAttendanceRequest;
import com.github.k1mb1.vkr_backend.journal.service.dto.request.UpsertAttendanceRequest;
import com.github.k1mb1.vkr_backend.journal.service.dto.response.AttendanceCellResponse;
import com.github.k1mb1.vkr_backend.journal.service.dto.response.AttendanceSummaryResponse;
import com.github.k1mb1.vkr_backend.journal.service.dto.response.AttendanceTableResponse;
import com.github.k1mb1.vkr_backend.journal.service.dto.response.JournalAudienceScopeResponse;
import com.github.k1mb1.vkr_backend.lesson.api.LessonStudentResponse;
import com.github.k1mb1.vkr_backend.lesson.api.LessonStudentsApi;
import com.github.k1mb1.vkr_backend.lesson.domain.LessonEntity;
import com.github.k1mb1.vkr_backend.lesson.domain.LessonScopeEntity;
import com.github.k1mb1.vkr_backend.subject.api.AttendanceHighlightPolicyResponse;
import com.github.k1mb1.vkr_backend.teacher.domain.TeacherSubjectPermissionEntity;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceService {

    final AttendanceRepository attendanceRepository;

    final AttendanceMapper attendanceMapper;

    final JournalPermissionRepository permissionRepository;

    final JournalLessonRepository lessonRepository;

    final JournalLessonScopeRepository lessonScopeRepository;

    final JournalStudentRefRepository studentRefRepository;

    final LessonStudentsApi lessonStudentsApi;

    final JournalAudienceService audienceService;

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

        var lessons = lessonRepository.resolveLessons(
                lessonScopeRepository, permission, filter.lessonScopeId(), filter.lessonId());
        var scopes = resolveScopes(lessons, permission, filter);

        var students = lessonStudentsApi.studentsOfScopes(
                scopes.stream().map(LessonScopeEntity::getId).toList());
        var audience = audienceService.audienceOf(permission);
        var highlightPolicy = attendanceMapper.toHighlightPolicyResponse(
                permission.getSubject().getAttendanceHighlightPolicy());

        return buildTable(highlightPolicy, audience, students, scopes);
    }

    private List<LessonScopeEntity> resolveScopes(
            List<LessonEntity> lessons, TeacherSubjectPermissionEntity permission, AttendanceFilter filter) {
        var visible = audienceService.visibleScopesIn(lessons, permission);
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
            List<JournalAudienceScopeResponse> audience,
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

    public Map<UUID, AttendanceSummaryResponse> summarize(
            Collection<UUID> lessonScopeIds, Collection<UUID> studentIds) {
        if (lessonScopeIds.isEmpty() || studentIds.isEmpty()) {
            return Map.of();
        }
        var rows = attendanceRepository.findByLessonScopeIdInAndStudentIdIn(lessonScopeIds, studentIds);
        // Счётчик по каждому статусу посещаемости на студента.
        Map<UUID, Map<AttendanceStatus, Integer>> counts = new HashMap<>();
        for (var a : rows) {
            counts.computeIfAbsent(a.getStudent().getId(), k -> new EnumMap<>(AttendanceStatus.class))
                    .merge(a.getStatus(), 1, Integer::sum);
        }
        var result = new HashMap<UUID, AttendanceSummaryResponse>();
        counts.forEach((id, c) -> result.put(
                id,
                AttendanceSummaryResponse.builder()
                        .present(c.getOrDefault(AttendanceStatus.PRESENT, 0))
                        .late(c.getOrDefault(AttendanceStatus.LATE, 0))
                        .absent(c.getOrDefault(AttendanceStatus.ABSENT, 0))
                        .excused(c.getOrDefault(AttendanceStatus.EXCUSED, 0))
                        .build()));
        return result;
    }
}
