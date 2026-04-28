package com.github.k1mb1.vkr_backend.domain.student_attendances;

import com.github.k1mb1.vkr_backend.domain.lessons.LessonEntity;
import com.github.k1mb1.vkr_backend.domain.lessons.LessonRepository;
import com.github.k1mb1.vkr_backend.domain.student_attendances.requests.UpsertAttendanceRequest;
import com.github.k1mb1.vkr_backend.domain.student_attendances.responses.AttendanceEntryResponse;
import com.github.k1mb1.vkr_backend.domain.student_attendances.responses.SubjectAttendanceTableResponse;
import com.github.k1mb1.vkr_backend.domain.student_attendances.responses.SubjectAttendanceTableResponse.StudentAttendanceTableResponse;
import com.github.k1mb1.vkr_backend.domain.student_attendances.responses.SubjectAttendanceTableResponse.SubjectLessonTableEntryResponse;
import com.github.k1mb1.vkr_backend.domain.students.StudentEntity;
import com.github.k1mb1.vkr_backend.domain.students.StudentRepository;
import com.github.k1mb1.vkr_backend.domain.subjects.SubjectRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
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

    public SubjectAttendanceTableResponse findBySubjectId(UUID subjectId) {
        var subject = subjectRepository.findById(subjectId)
            .orElseThrow(() -> new EntityNotFoundException("Subject not found: " + subjectId));

        var byStudent = attendanceRepository.findAllBySubjectId(subjectId).stream()
            .collect(Collectors.groupingBy(a -> a.getStudent().getId()));

        var lessons = subject.getLessons().stream()
            .sorted(Comparator.comparing(LessonEntity::getDateTime, Comparator.nullsLast(Comparator.naturalOrder())))
            .map(l -> new SubjectLessonTableEntryResponse(l.getId(), l.getName(), l.getDateTime()))
            .toList();

        var students = subject.getStudents().stream()
            .sorted(Comparator.comparing(StudentEntity::getUsername).thenComparing(StudentEntity::getId))
            .map(s -> new StudentAttendanceTableResponse(
                s.getId(),
                s.getUsername(),
                byStudent.getOrDefault(s.getId(), List.of()).stream()
                    .sorted(Comparator.comparing(a -> a.getLesson().getDateTime(), Comparator.nullsLast(Comparator.naturalOrder())))
                    .map(this::toEntry)
                    .toList()
            ))
            .toList();

        return new SubjectAttendanceTableResponse(lessons, students);
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
