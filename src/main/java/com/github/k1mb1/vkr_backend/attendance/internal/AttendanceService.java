package com.github.k1mb1.vkr_backend.attendance.internal;

import com.github.k1mb1.vkr_backend.attendance.AttendanceApi;
import com.github.k1mb1.vkr_backend.attendance.AttendanceSummary;
import com.github.k1mb1.vkr_backend.attendance.domain.Attendance;
import com.github.k1mb1.vkr_backend.attendance.domain.AttendanceStatus;
import com.github.k1mb1.vkr_backend.attendance.web.filters.AttendanceFilter;
import com.github.k1mb1.vkr_backend.attendance.web.requests.BulkUpsertAttendanceRequest;
import com.github.k1mb1.vkr_backend.attendance.web.requests.UpsertAttendanceRequest;
import com.github.k1mb1.vkr_backend.attendance.web.responses.AttendanceAudienceScope;
import com.github.k1mb1.vkr_backend.attendance.web.responses.AttendanceCellResponse;
import com.github.k1mb1.vkr_backend.attendance.web.responses.AttendanceTableResponse;
import com.github.k1mb1.vkr_backend.common.error.ResourceNotFoundException;
import com.github.k1mb1.vkr_backend.lesson.LessonStudentsApi;
import com.github.k1mb1.vkr_backend.lesson.domain.Lesson;
import com.github.k1mb1.vkr_backend.lesson.domain.LessonScope;
import com.github.k1mb1.vkr_backend.lesson.internal.LessonResolver;
import com.github.k1mb1.vkr_backend.lesson.internal.LessonScopeRepository;
import com.github.k1mb1.vkr_backend.lesson.internal.LessonSpecifications;
import com.github.k1mb1.vkr_backend.student.domain.Student;
import com.github.k1mb1.vkr_backend.student.internal.StudentRepository;
import com.github.k1mb1.vkr_backend.subject.domain.TeacherSubjectPermission;
import com.github.k1mb1.vkr_backend.subject.internal.SubjectMapper;
import com.github.k1mb1.vkr_backend.subject.internal.TeacherSubjectPermissionRepository;
import com.github.k1mb1.vkr_backend.subject.web.responses.AttendanceHighlightPolicyResponse;
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
class AttendanceService implements AttendanceApi {

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

    private List<LessonScope> resolveScopes(
            List<Lesson> lessons, TeacherSubjectPermission permission, AttendanceFilter filter) {
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
            List<Student> students,
            List<LessonScope> scopes) {
        var studentIds = students.stream().map(Student::getId).toList();
        var scopeIds = scopes.stream().map(LessonScope::getId).toList();

        var attendances = studentIds.isEmpty() || scopeIds.isEmpty()
                ? List.<Attendance>of()
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
        var existingByKey = new HashMap<String, Attendance>();
        for (var a : existing) {
            existingByKey.put(a.getStudent().getId() + "|" + a.getLessonScope().getId(), a);
        }

        var saved = new ArrayList<Attendance>(items.size());
        for (var item : items) {
            var key = item.studentId() + "|" + item.lessonScopeId();
            var attendance = existingByKey.computeIfAbsent(
                    key,
                    k -> Attendance.builder()
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

    private List<LessonScope> visibleScopesIn(List<Lesson> lessons, TeacherSubjectPermission permission) {
        var result = new ArrayList<LessonScope>();
        for (var lesson : lessons) {
            result.addAll(LessonSpecifications.visibleScopes(lesson, permission));
        }
        result.sort(Comparator.comparing(
                        (LessonScope s) -> s.getStartedAt(), Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(s -> s.getLesson().getOrderIndex()));
        return result;
    }

    private List<AttendanceAudienceScope> audienceOf(TeacherSubjectPermission permission) {
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
