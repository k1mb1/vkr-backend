package com.github.k1mb1.vkr_backend.attendance.internal;

import com.github.k1mb1.vkr_backend.attendance.AttendanceApi;
import com.github.k1mb1.vkr_backend.attendance.AttendanceSummary;
import com.github.k1mb1.vkr_backend.attendance.domain.Attendance;
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

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class AttendanceService
    implements AttendanceApi {

    final AttendanceRepository attendanceRepository;

    final AttendanceMapper attendanceMapper;

    final TeacherSubjectPermissionRepository permissionRepository;

    final LessonRepository lessonRepository;

    final LessonScopeRepository lessonScopeRepository;

    final StudentRepository studentRepository;

    final LessonStudentsApi lessonStudentsApi;

    @Override
    public AttendanceTableResponse getAttendanceTable(AttendanceFilter filter) {
        var permission = permissionRepository.findWithDetailsById(filter.permissionId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "TeacherSubjectPermission",
                filter.permissionId()
            ));

        var lessons = resolveLessons(permission, filter);
        var scopes = resolveScopes(lessons, permission, filter);

        var students = unionStudentsAcrossScopes(scopes);
        var audience = audienceOf(permission);

        return buildTable(audience, students, scopes);
    }

    private List<Lesson> resolveLessons(
        TeacherSubjectPermission permission,
        AttendanceFilter filter
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

    private List<LessonScope> resolveScopes(
        List<Lesson> lessons,
        TeacherSubjectPermission permission,
        AttendanceFilter filter
    ) {
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

    private AttendanceTableResponse buildTable(
        List<AttendanceAudienceScope> audience,
        List<Student> students,
        List<LessonScope> scopes
    ) {
        var studentIds = students.stream().map(Student::getId).toList();
        var scopeIds = scopes.stream().map(LessonScope::getId).toList();

        var attendances = studentIds.isEmpty() || scopeIds.isEmpty()
                          ? List.<Attendance>of()
                          : attendanceRepository.findByLessonScopeIdInAndStudentIdIn(
                              scopeIds,
                              studentIds
                          );

        return new AttendanceTableResponse(
            audience,
            students.stream().map(attendanceMapper::toTableStudent).toList(),
            scopes.stream().map(attendanceMapper::toTableLesson).toList(),
            attendances.stream().map(attendanceMapper::toCell).toList()
        );
    }

    @Transactional
    @Override
    public List<AttendanceCellResponse> upsertAll(BulkUpsertAttendanceRequest request) {
        var items = request.items();
        var seen = new HashSet<String>();
        for (var item : items) {
            var key = item.studentId() + "|" + item.lessonScopeId();
            if (!seen.add(key)) {
                throw new IllegalArgumentException(
                    "Duplicate (studentId, lessonScopeId) in request: " + item.studentId() + ", " + item.lessonScopeId());
            }
        }

        var studentIds = items.stream().map(UpsertAttendanceRequest::studentId).distinct().toList();
        var scopeIds = items.stream().map(UpsertAttendanceRequest::lessonScopeId).distinct().toList();
        var existing = attendanceRepository.findByLessonScopeIdInAndStudentIdIn(scopeIds, studentIds);
        var existingByKey = new HashMap<String, Attendance>();
        for (var a : existing) {
            existingByKey.put(a.getStudent().getId() + "|" + a.getLessonScope().getId(), a);
        }

        var saved = new ArrayList<Attendance>(items.size());
        for (var item : items) {
            var key = item.studentId() + "|" + item.lessonScopeId();
            var attendance = existingByKey.computeIfAbsent(key, k -> Attendance.builder()
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
    public Map<UUID, AttendanceSummary> summarize(
        Collection<UUID> lessonScopeIds,
        Collection<UUID> studentIds
    ) {
        if (lessonScopeIds.isEmpty() || studentIds.isEmpty()) {
            return Map.of();
        }
        var rows = attendanceRepository.findByLessonScopeIdInAndStudentIdIn(lessonScopeIds, studentIds);
        // [present, late, absent, excused] на студента
        var counts = new HashMap<UUID, int[]>();
        for (var a : rows) {
            var c = counts.computeIfAbsent(a.getStudent().getId(), k -> new int[4]);
            switch (a.getStatus()) {
                case PRESENT -> c[0]++;
                case LATE -> c[1]++;
                case ABSENT -> c[2]++;
                case EXCUSED -> c[3]++;
            }
        }
        var result = new HashMap<UUID, AttendanceSummary>();
        counts.forEach((id, c) -> result.put(id, new AttendanceSummary(c[0], c[1], c[2], c[3])));
        return result;
    }

    private List<LessonScope> visibleScopesIn(
        List<Lesson> lessons,
        TeacherSubjectPermission permission
    ) {
        var result = new ArrayList<LessonScope>();
        for (var lesson : lessons) {
            result.addAll(LessonSpecifications.visibleScopes(lesson, permission));
        }
        result.sort(Comparator.comparing(
                (LessonScope s) -> s.getStartedAt(),
                Comparator.nullsLast(Comparator.naturalOrder())
            )
                        .thenComparing(s -> s.getLesson().getOrderIndex()));
        return result;
    }

    private List<Student> unionStudentsAcrossScopes(List<LessonScope> scopes) {
        var seen = new LinkedHashMap<UUID, Student>();
        for (var scope : scopes) {
            for (var s : lessonStudentsApi.studentsOf(scope)) {
                seen.putIfAbsent(s.getId(), s);
            }
        }
        var result = new ArrayList<>(seen.values());
        result.sort(Comparator.comparing(Student::getUsername));
        return result;
    }

    private List<AttendanceAudienceScope> audienceOf(TeacherSubjectPermission permission) {
        if (LessonSpecifications.permissionAllowsAllGroups(permission)) {
            return List.of();
        }
        return permission.getScopes()
            .stream()
            .sorted(Comparator.comparing((PermissionScope s) -> s.getGroup().getName())
                        .thenComparing(s -> s.getAllowedSubgroup() == null
                                            ? -1
                                            : s.getAllowedSubgroup().getIndex()))
            .map(s -> new AttendanceAudienceScope(
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
