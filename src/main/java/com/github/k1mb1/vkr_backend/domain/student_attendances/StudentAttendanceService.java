package com.github.k1mb1.vkr_backend.domain.student_attendances;

import com.github.k1mb1.vkr_backend.domain.lessons.LessonRepository;
import com.github.k1mb1.vkr_backend.domain.student_attendances.requests.UpsertAttendanceRequest;
import com.github.k1mb1.vkr_backend.domain.student_attendances.responses.AttendanceEntryResponse;
import com.github.k1mb1.vkr_backend.domain.student_attendances.responses.StudentAttendanceTableResponse;
import com.github.k1mb1.vkr_backend.domain.students.StudentEntity;
import com.github.k1mb1.vkr_backend.domain.students.StudentRepository;
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

    public List<StudentAttendanceTableResponse> findBySubjectId(UUID subjectId) {
        List<StudentAttendanceEntity> rows = attendanceRepository.findAllBySubjectId(subjectId);

        Map<UUID, List<StudentAttendanceEntity>> byStudent = rows.stream()
            .collect(Collectors.groupingBy(a -> a.getStudent().getId()));

        return byStudent.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(e -> {
                StudentEntity student = e.getValue().get(0).getStudent();
                List<AttendanceEntryResponse> entries = e.getValue().stream()
                    .sorted(Comparator.comparing(
                        a -> a.getLesson().getDateTime(),
                        Comparator.nullsLast(Comparator.naturalOrder())
                    ))
                    .map(this::toEntry)
                    .toList();
                return new StudentAttendanceTableResponse(
                    student.getId(),
                    student.getUsername(),
                    entries
                );
            })
            .toList();
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
}
