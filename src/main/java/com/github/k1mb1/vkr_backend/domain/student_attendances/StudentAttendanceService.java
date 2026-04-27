package com.github.k1mb1.vkr_backend.domain.student_attendances;

import com.github.k1mb1.vkr_backend.domain.lessons.LessonRepository;
import com.github.k1mb1.vkr_backend.domain.student_attendances.requests.UpsertAttendanceRequest;
import com.github.k1mb1.vkr_backend.domain.student_attendances.responses.AttendanceEntryResponse;
import com.github.k1mb1.vkr_backend.domain.student_attendances.responses.StudentAttendanceTableResponse;
import com.github.k1mb1.vkr_backend.domain.student_attendances.responses.SubjectAttendanceTableResponse;
import com.github.k1mb1.vkr_backend.domain.students.StudentEntity;
import com.github.k1mb1.vkr_backend.domain.students.StudentRepository;
import com.github.k1mb1.vkr_backend.domain.subjects.SubjectRepository;
import com.github.k1mb1.vkr_backend.domain.subjects.responses.SubjectLessonTableEntryResponse;
import jakarta.persistence.EntityNotFoundException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
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

        Map<UUID, List<StudentAttendanceEntity>> byStudent = attendanceRepository
            .findAllBySubjectId(subjectId)
            .stream()
            .collect(Collectors.groupingBy(a -> a.getStudent().getId()));

        var students = subject.getStudents().stream()
            .sorted(
                Comparator.comparing(StudentEntity::getUsername)
                    .thenComparing(StudentEntity::getId)
            )
            .map(student -> new StudentAttendanceTableResponse(
                student.getId(),
                student.getUsername(),
                byStudent.getOrDefault(student.getId(), List.of()).stream()
                    .sorted(Comparator.comparing(
                        a -> a.getLesson().getDateTime(),
                        Comparator.nullsLast(Comparator.naturalOrder())
                    ))
                    .map(this::toEntry)
                    .toList()
            ))
            .toList();

        var lessons = subject.getLessons().stream()
            .sorted(
                Comparator.comparing(
                    StudentAttendanceService::lessonDateTime,
                    Comparator.nullsLast(Comparator.naturalOrder())
                )
                .thenComparing(l -> l.getId())
            )
            .map(lesson -> new SubjectLessonTableEntryResponse(
                lesson.getId(),
                lesson.getName(),
                lesson.getDateTime()
            ))
            .toList();

        return new SubjectAttendanceTableResponse(lessons, students);
    }

    @Transactional
    public AttendanceEntryResponse upsert(UUID lessonId, UpsertAttendanceRequest request) {
        var lesson = lessonRepository.findById(lessonId)
            .orElseThrow(() -> new EntityNotFoundException("Lesson not found: " + lessonId));

        var student = studentRepository.findById(request.studentId())
            .orElseThrow(() -> new EntityNotFoundException(
                "Student not found: " + request.studentId()
            ));

        var attendance = attendanceRepository
            .findByLesson_IdAndStudent_Id(lessonId, request.studentId())
            .orElseGet(() -> StudentAttendanceEntity.builder()
                .lesson(lesson)
                .student(student)
                .build()
            );

        attendance.setPresence(request.presence());
        if (request.note() != null) {
            attendance.setNote(request.note());
        }

        return toEntry(attendanceRepository.save(attendance));
    }

    private AttendanceEntryResponse toEntry(StudentAttendanceEntity a) {
        return new AttendanceEntryResponse(
            a.getId(),
            a.getLesson().getId(),
            a.getPresence(),
            a.getNote()
        );
    }

    private static java.time.OffsetDateTime lessonDateTime(
        com.github.k1mb1.vkr_backend.domain.lessons.LessonEntity lesson
    ) {
        return lesson.getDateTime();
    }
}
