package com.github.k1mb1.vkr_backend.attendance.internal;

import com.github.k1mb1.vkr_backend.attendance.AttendanceApi;
import com.github.k1mb1.vkr_backend.attendance.domain.Attendance;
import com.github.k1mb1.vkr_backend.attendance.web.filters.AttendanceFilter;
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

        var lessons = lessonRepository.findAll(LessonSpecifications.forPermission(permission));
        var scopes = visibleScopesIn(lessons, permission);

        var students = unionStudentsAcrossScopes(scopes);
        var audience = audienceOf(permission);

        return buildTable(audience, students, scopes);
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
    public AttendanceCellResponse upsert(UpsertAttendanceRequest request) {
        var attendance = attendanceRepository.findByStudentIdAndLessonScopeId(
                request.studentId(),
                request.lessonScopeId()
            )
            .orElseGet(() -> Attendance.builder()
                .student(studentRepository.getReferenceById(request.studentId()))
                .lessonScope(lessonScopeRepository.getReferenceById(request.lessonScopeId()))
                .build());

        attendance.setStatus(request.status());
        attendance.setComment(request.comment());

        return attendanceMapper.toCell(attendanceRepository.save(attendance));
    }

    private List<LessonScope> visibleScopesIn(
        List<Lesson> lessons,
        TeacherSubjectPermission permission
    ) {
        var permittedGroupIds = LessonSpecifications.permissionGroupIds(permission);
        var subgroupRestrictions = subgroupRestrictionsByGroup(permission);
        var result = new ArrayList<LessonScope>();
        for (var lesson : lessons) {
            for (var scope : lesson.getScopes()) {
                if (scope.isAllGroups()) {
                    result.add(scope);
                    continue;
                }
                if (scope.getGroup() == null) {
                    continue;
                }
                if (permission.isAllPermissions() || permittedGroupIds.contains(scope.getGroup()
                                                                                    .getId())) {
                    var allowedSubgroupId = subgroupRestrictions.get(scope.getGroup().getId());
                    if (allowedSubgroupId == null || scope.getAllowedSubgroup() == null || allowedSubgroupId.equals(
                        scope.getAllowedSubgroup().getId())) {
                        result.add(scope);
                    }
                }
            }
        }
        result.sort(Comparator.comparing(
                (LessonScope s) -> s.getStartedAt(),
                Comparator.nullsLast(Comparator.naturalOrder())
            )
                        .thenComparing(s -> s.getLesson().getOrderIndex()));
        return result;
    }

    private Map<UUID, UUID> subgroupRestrictionsByGroup(TeacherSubjectPermission permission) {
        if (permission.isAllPermissions()) {
            return Map.of();
        }
        var map = new HashMap<UUID, UUID>();
        for (var ps : permission.getScopes()) {
            if (ps.getAllowedSubgroup() != null) {
                map.putIfAbsent(ps.getGroup().getId(), ps.getAllowedSubgroup().getId());
            } else {
                map.put(ps.getGroup().getId(), null);
            }
        }
        return map;
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
        if (permission.isAllPermissions()) {
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
