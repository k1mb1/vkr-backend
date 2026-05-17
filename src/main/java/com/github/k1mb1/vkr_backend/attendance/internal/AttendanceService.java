package com.github.k1mb1.vkr_backend.attendance.internal;

import com.github.k1mb1.vkr_backend.attendance.AttendanceApi;
import com.github.k1mb1.vkr_backend.attendance.domain.Attendance;
import com.github.k1mb1.vkr_backend.attendance.web.filters.AttendanceFilter;
import com.github.k1mb1.vkr_backend.attendance.web.requests.UpsertAttendanceRequest;
import com.github.k1mb1.vkr_backend.attendance.web.responses.AttendanceCellResponse;
import com.github.k1mb1.vkr_backend.attendance.web.responses.AttendanceTableLesson;
import com.github.k1mb1.vkr_backend.attendance.web.responses.AttendanceTableResponse;
import com.github.k1mb1.vkr_backend.attendance.web.responses.AttendanceTableStudent;
import com.github.k1mb1.vkr_backend.lesson.domain.Lesson;
import com.github.k1mb1.vkr_backend.lesson.internal.LessonRepository;
import com.github.k1mb1.vkr_backend.lesson.internal.LessonSpecifications;
import com.github.k1mb1.vkr_backend.student.domain.Student;
import com.github.k1mb1.vkr_backend.student.internal.StudentRepository;
import com.github.k1mb1.vkr_backend.subject.domain.TeacherSubjectPermission;
import com.github.k1mb1.vkr_backend.subject.internal.TeacherSubjectPermissionRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class AttendanceService implements AttendanceApi {

    final AttendanceRepository attendanceRepository;

    final AttendanceMapper attendanceMapper;

    final TeacherSubjectPermissionRepository permissionRepository;

    final LessonRepository lessonRepository;

    final StudentRepository studentRepository;

    @Override
    public AttendanceTableResponse getAttendanceTable(AttendanceFilter filter) {
        var permission = permissionRepository
            .findById(filter.permissionId())
            .orElseThrow(() ->
                new EntityNotFoundException(
                    "TeacherSubjectPermission not found: " +
                        filter.permissionId()
                )
            );

        var students = loadStudents(permission);
        var lessons = lessonRepository
            .findAll(new LessonSpecifications(permission).toSpecification())
            .stream()
            .sorted(Comparator.comparing(Lesson::getStartedAt))
            .toList();

        var studentIds = students.stream().map(Student::getId).toList();
        var lessonIds = lessons.stream().map(Lesson::getId).toList();

        var attendances =
            studentIds.isEmpty() || lessonIds.isEmpty()
                ? List.<Attendance>of()
                : attendanceRepository.findByLessonIdInAndStudentIdIn(
                      lessonIds,
                      studentIds
                  );

        return new AttendanceTableResponse(
            students.stream().map(attendanceMapper::toTableStudent).toList(),
            lessons.stream().map(attendanceMapper::toTableLesson).toList(),
            attendances.stream().map(attendanceMapper::toCell).toList()
        );
    }

    @Transactional
    @Override
    public AttendanceCellResponse upsert(UpsertAttendanceRequest request) {
        var attendance = attendanceRepository
            .findByStudentIdAndLessonId(request.studentId(), request.lessonId())
            .orElseGet(() ->
                Attendance.builder()
                    .student(
                        studentRepository.getReferenceById(request.studentId())
                    )
                    .lesson(
                        lessonRepository.getReferenceById(request.lessonId())
                    )
                    .build()
            );

        attendance.setStatus(request.status());
        attendance.setComment(request.comment());

        return attendanceMapper.toCell(attendanceRepository.save(attendance));
    }

    private List<Student> loadStudents(TeacherSubjectPermission permission) {
        var groupId = permission.getGroup().getId();
        var students =
            permission.getAllowedSubgroup() != null
                ? studentRepository.findByGroupIdAndSubgroupIdAndArchivedAtIsNull(
                      groupId,
                      permission.getAllowedSubgroup().getId()
                  )
                : studentRepository.findByGroupIdAndArchivedAtIsNull(groupId);
        return students
            .stream()
            .sorted(Comparator.comparing(Student::getUsername))
            .toList();
    }
}
