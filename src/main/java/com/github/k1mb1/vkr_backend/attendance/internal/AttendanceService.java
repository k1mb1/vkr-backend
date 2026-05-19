package com.github.k1mb1.vkr_backend.attendance.internal;

import com.github.k1mb1.vkr_backend.attendance.AttendanceApi;
import com.github.k1mb1.vkr_backend.attendance.domain.Attendance;
import com.github.k1mb1.vkr_backend.attendance.web.filters.AttendanceFilter;
import com.github.k1mb1.vkr_backend.attendance.web.requests.UpsertAttendanceRequest;
import com.github.k1mb1.vkr_backend.attendance.web.responses.AttendanceAudienceScope;
import com.github.k1mb1.vkr_backend.attendance.web.responses.AttendanceCellResponse;
import com.github.k1mb1.vkr_backend.attendance.web.responses.AttendanceTableResponse;
import com.github.k1mb1.vkr_backend.lesson.LessonStudentsApi;
import com.github.k1mb1.vkr_backend.lesson.domain.Lesson;
import com.github.k1mb1.vkr_backend.lesson.internal.LessonRepository;
import com.github.k1mb1.vkr_backend.lesson.internal.LessonSpecifications;
import com.github.k1mb1.vkr_backend.student.domain.Student;
import com.github.k1mb1.vkr_backend.student.internal.StudentRepository;
import com.github.k1mb1.vkr_backend.subject.domain.TeacherSubjectPermission;
import com.github.k1mb1.vkr_backend.subject.internal.TeacherSubjectPermissionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class AttendanceService
    implements AttendanceApi {

    final AttendanceRepository attendanceRepository;

    final AttendanceMapper attendanceMapper;

    final TeacherSubjectPermissionRepository permissionRepository;

    final LessonRepository lessonRepository;

    final StudentRepository studentRepository;

    final LessonStudentsApi lessonStudentsApi;

    @Override
    public AttendanceTableResponse getAttendanceTable(AttendanceFilter filter) {
        var permission = permissionRepository.findByIdWithDetails(filter.permissionId())
            .orElseThrow(() -> new EntityNotFoundException(
                "TeacherSubjectPermission not found: " + filter.permissionId()));

        var lessons = lessonRepository.findAll(
                LessonSpecifications.forPermission(permission),
                Sort.by("startedAt"))
            .stream()
            .toList();

        var students = unionStudentsAcross(lessons);
        var audience = audienceOf(permission);

        return buildTable(audience, students, lessons);
    }

    private AttendanceTableResponse buildTable(
        List<AttendanceAudienceScope> audience,
        List<Student> students,
        List<Lesson> lessons
    ) {
        var studentIds = students.stream().map(Student::getId).toList();
        var lessonIds = lessons.stream().map(Lesson::getId).toList();

        var attendances = studentIds.isEmpty() || lessonIds.isEmpty()
                          ? List.<Attendance>of()
                          : attendanceRepository.findByLessonIdInAndStudentIdIn(
                              lessonIds,
                              studentIds
                          );

        return new AttendanceTableResponse(
            audience,
            students.stream().map(attendanceMapper::toTableStudent).toList(),
            lessons.stream().map(attendanceMapper::toTableLesson).toList(),
            attendances.stream().map(attendanceMapper::toCell).toList()
        );
    }

    @Transactional
    @Override
    public AttendanceCellResponse upsert(UpsertAttendanceRequest request) {
        var attendance = attendanceRepository.findByStudentIdAndLessonId(
                request.studentId(),
                request.lessonId()
            )
            .orElseGet(() -> Attendance.builder()
                .student(studentRepository.getReferenceById(request.studentId()))
                .lesson(lessonRepository.getReferenceById(request.lessonId()))
                .build());

        attendance.setStatus(request.status());
        attendance.setComment(request.comment());

        return attendanceMapper.toCell(attendanceRepository.save(attendance));
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

    private List<AttendanceAudienceScope> audienceOf(TeacherSubjectPermission permission) {
        if (permission.isAllPermissions()) {
            return permission.getSubject().getGroups().stream()
                .sorted(Comparator.comparing(g -> g.getName()))
                .map(g -> new AttendanceAudienceScope(g.getId(), g.getName(), null, null))
                .toList();
        }
        return permission.getScopes().stream()
            .sorted(Comparator
                .comparing((com.github.k1mb1.vkr_backend.subject.domain.PermissionScope s) -> s.getGroup().getName())
                .thenComparing(s -> s.getAllowedSubgroup() == null ? -1 : s.getAllowedSubgroup().getIndex()))
            .map(s -> new AttendanceAudienceScope(
                s.getGroup().getId(),
                s.getGroup().getName(),
                s.getAllowedSubgroup() != null ? s.getAllowedSubgroup().getId() : null,
                s.getAllowedSubgroup() != null ? s.getAllowedSubgroup().getIndex() : null
            ))
            .toList();
    }
}
