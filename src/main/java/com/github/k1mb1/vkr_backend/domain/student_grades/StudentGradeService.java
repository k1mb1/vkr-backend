package com.github.k1mb1.vkr_backend.domain.student_grades;

import com.github.k1mb1.vkr_backend.domain.lessons.LessonRepository;
import com.github.k1mb1.vkr_backend.domain.student_grades.responses.GradeEntryResponse;
import com.github.k1mb1.vkr_backend.domain.student_grades.responses.StudentGradesResponse;
import com.github.k1mb1.vkr_backend.domain.students.StudentRepository;
import com.github.k1mb1.vkr_backend.domain.subjects.SubjectService;
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
public class StudentGradeService {

    final StudentGradeRepository studentGradeRepository;
    final StudentRepository studentRepository;
    final LessonRepository lessonRepository;
    final SubjectService subjectService;

    public List<StudentGradesResponse> findGradesBySubject(UUID subjectId) {
        // Ensure subject exists
        subjectService.findEntityById(subjectId);

        // All students assigned to the subject
        var students = studentRepository.findAllBySubjects_Id(subjectId);

        if (students.isEmpty()) {
            return List.of();
        }

        // All lessons for this subject
        var lessons = lessonRepository.findAllBySubject_Id(subjectId);

        // All grades for these students in this subject
        var studentIds = students.stream()
            .map(s -> s.getId())
            .toList();

        // Map: studentId -> (lessonId -> grade entity)
        Map<UUID, Map<UUID, StudentGradeEntity>> gradesByStudentAndLesson =
            studentGradeRepository
                .findAllBySubjectIdAndStudentIdIn(subjectId, studentIds)
                .stream()
                .collect(Collectors.groupingBy(
                    g -> g.getStudent().getId(),
                    Collectors.toMap(
                        g -> g.getLesson().getId(),
                        g -> g
                    )
                ));

        return students.stream()
            .map(student -> {
                var gradeMap = gradesByStudentAndLesson.getOrDefault(
                    student.getId(),
                    Map.of()
                );

                var gradeEntries = lessons.stream()
                    .map(lesson -> {
                        var grade = gradeMap.get(lesson.getId());
                        return new GradeEntryResponse(
                            lesson.getId(),
                            lesson.getName(),
                            lesson.getType(),
                            lesson.getDateTime(),
                            grade != null ? grade.getValue() : null,
                            grade != null ? grade.getComment() : null
                        );
                    })
                    .toList();

                return new StudentGradesResponse(
                    student.getId(),
                    student.getUsername(),
                    gradeEntries
                );
            })
            .toList();
    }
}
