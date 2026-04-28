package com.github.k1mb1.vkr_backend.domain.student_attendances;

import com.github.k1mb1.vkr_backend.domain.lessons.LessonFilter;
import com.github.k1mb1.vkr_backend.domain.lessons.LessonRepository;
import com.github.k1mb1.vkr_backend.domain.student_attendances.filters.AttendanceFilter;
import com.github.k1mb1.vkr_backend.domain.student_attendances.filters.FindAttendanceFilter;
import com.github.k1mb1.vkr_backend.domain.student_attendances.requests.UpsertAttendanceRequest;
import com.github.k1mb1.vkr_backend.domain.student_attendances.responses.AttendanceCellResponse;
import com.github.k1mb1.vkr_backend.domain.student_attendances.responses.AttendanceEntryResponse;
import com.github.k1mb1.vkr_backend.domain.student_attendances.responses.SubjectAttendanceTableResponse;
import com.github.k1mb1.vkr_backend.domain.students.StudentEntity;
import com.github.k1mb1.vkr_backend.domain.students.StudentRepository;
import com.github.k1mb1.vkr_backend.domain.students.responses.StudentEntryResponse;
import com.github.k1mb1.vkr_backend.domain.subjects.SubjectRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.Comparator;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentAttendanceService {

    final StudentAttendanceRepository attendanceRepository;
    final StudentRepository studentRepository;
    final LessonRepository lessonRepository;
    final SubjectRepository subjectRepository;

    public SubjectAttendanceTableResponse findBySubjectId(UUID subjectId, FindAttendanceFilter filter) {
        var subject = subjectRepository.findById(subjectId)
            .orElseThrow(() -> new EntityNotFoundException("Subject not found: " + subjectId));

        var lessonFilter = LessonFilter.builder()
            .subjectId(subjectId)
            .lessonType(filter.lessonType())
            .groupId(filter.groupId())
            .build();

        var lessons = lessonRepository.findAll(lessonFilter.toSpecification(), Sort.by(Sort.Direction.ASC, "dateTime"))
            .stream()
            .map(l -> new SubjectAttendanceTableResponse.SubjectLessonTableEntryResponse(
                l.getId(),
                l.getName(),
                l.getDateTime(),
                l.getType(),
                l.getGroup() != null ? l.getGroup().getId() : null
            ))
            .toList();

        var attendanceFilter = new AttendanceFilter(subjectId, filter.lessonType(), filter.groupId());

        var attendances = attendanceRepository.findAll(attendanceFilter.toSpecification(), Sort.by(Sort.Direction.ASC, "lesson.dateTime"))
            .stream()
            .map(a -> new AttendanceCellResponse(
                a.getId(),
                a.getLesson().getId(),
                a.getStudent().getId(),
                a.getPresence(),
                a.getNote()
            ))
            .toList();

        var students = subject.getStudents().stream()
            .sorted(Comparator.comparing(StudentEntity::getUsername).thenComparing(StudentEntity::getId))
            .map(s -> new StudentEntryResponse(s.getId(), s.getUsername()))
            .toList();

        return new SubjectAttendanceTableResponse(lessons, students, attendances);
    }

    @Transactional
    public AttendanceEntryResponse upsert(UUID lessonId, UpsertAttendanceRequest request) {
        var lesson = lessonRepository.findById(lessonId)
            .orElseThrow(() -> new EntityNotFoundException("Lesson not found: " + lessonId));

        var student = studentRepository.findById(request.studentId())
            .orElseThrow(() -> new EntityNotFoundException("Student not found: " + request.studentId()));

        var attendance = attendanceRepository
            .findByLesson_IdAndStudent_Id(lessonId, request.studentId())
            .orElseGet(() -> StudentAttendanceEntity.builder().lesson(lesson).student(student).build());

        attendance.setPresence(request.presence());
        if (request.note() != null) attendance.setNote(request.note());

        return toEntry(attendanceRepository.save(attendance));
    }

    private AttendanceEntryResponse toEntry(StudentAttendanceEntity a) {
        return new AttendanceEntryResponse(a.getId(), a.getLesson().getId(), a.getPresence(), a.getNote());
    }
}
