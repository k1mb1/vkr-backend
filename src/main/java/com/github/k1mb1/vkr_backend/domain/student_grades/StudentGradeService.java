package com.github.k1mb1.vkr_backend.domain.student_grades;

import com.github.k1mb1.vkr_backend.domain.lessons.LessonRepository;
import com.github.k1mb1.vkr_backend.domain.student_grades.responses.GradeEntryResponse;
import com.github.k1mb1.vkr_backend.domain.student_grades.responses.StudentGradesResponse;
import com.github.k1mb1.vkr_backend.domain.students.StudentEntity;
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

    /**
     * Returns grades for every student enrolled in a subject, grouped by student.
     *
     * <p>Group-awareness rules (mirrors attendance semantics):
     * <ul>
     *   <li>A student is included in a lesson's grade list when
     *       {@code lesson.group == null} (lecture — whole cohort) <b>OR</b>
     *       when {@code lesson.group.id == student.group.id} (practice —
     *       student belongs to the targeted subgroup).</li>
     *   <li>Only grades whose lesson passes the filter above are returned for
     *       each student.</li>
     * </ul>
     */
    public List<StudentGradesResponse> findGradesBySubject(UUID subjectId) {
        // All students enrolled in the subject.
        List<StudentEntity> students = studentRepository.findAllBySubjects_Id(subjectId);

        if (students.isEmpty()) {
            return List.of();
        }

        List<UUID> studentIds = students.stream()
            .map(StudentEntity::getId)
            .toList();

        // All grade rows for this subject × these students.
        List<StudentGradeEntity> allGrades =
            studentGradeRepository.findAllBySubjectIdAndStudentIdIn(subjectId, studentIds);

        // Index grades by student id for fast lookup.
        Map<UUID, List<StudentGradeEntity>> gradesByStudent = allGrades.stream()
            .collect(Collectors.groupingBy(g -> g.getStudent().getId()));

        return students.stream()
            .sorted(java.util.Comparator.comparing(StudentEntity::getUsername))
            .map(student -> {
                List<StudentGradeEntity> studentGrades =
                    gradesByStudent.getOrDefault(student.getId(), List.of());

                // Apply group-awareness filter: keep only grades where the
                // lesson targets the whole cohort or this student's subgroup.
                List<GradeEntryResponse> entries = studentGrades.stream()
                    .filter(g -> isLessonVisibleForStudent(g.getLesson(), student))
                    .sorted(java.util.Comparator.comparing(
                        g -> g.getLesson().getDateTime() != null
                            ? g.getLesson().getDateTime()
                            : java.time.OffsetDateTime.MIN
                    ))
                    .map(g -> new GradeEntryResponse(
                        g.getLesson().getId(),
                        g.getLesson().getName(),
                        g.getLesson().getType(),
                        g.getLesson().getDateTime(),
                        g.getValue(),
                        g.getComment()
                    ))
                    .toList();

                return new StudentGradesResponse(
                    student.getId(),
                    student.getUsername(),
                    entries
                );
            })
            .toList();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * A lesson is "visible" for a student when:
     * <ul>
     *   <li>the lesson has no group ({@code group == null}) — whole-cohort
     *       lesson such as a lecture, OR</li>
     *   <li>the lesson's group id matches the student's own group id.</li>
     * </ul>
     */
    private boolean isLessonVisibleForStudent(
        com.github.k1mb1.vkr_backend.domain.lessons.LessonEntity lesson,
        StudentEntity student
    ) {
        if (lesson.getGroup() == null) {
            // Whole-cohort lesson (e.g. lecture) — always visible.
            return true;
        }
        if (student.getGroup() == null) {
            // Student has no group — cannot be in a subgroup-targeted lesson.
            return false;
        }
        return lesson.getGroup().getId().equals(student.getGroup().getId());
    }
}
