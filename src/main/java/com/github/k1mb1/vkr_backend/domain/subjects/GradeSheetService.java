package com.github.k1mb1.vkr_backend.domain.subjects;

import com.github.k1mb1.vkr_backend.domain.lessons.LessonEntity;
import com.github.k1mb1.vkr_backend.domain.lessons.LessonRepository;
import com.github.k1mb1.vkr_backend.domain.lessons.LessonTaskEntity;
import com.github.k1mb1.vkr_backend.domain.student_attendances.StudentAttendanceRepository;
import com.github.k1mb1.vkr_backend.domain.student_grades.StudentTaskGradeEntity;
import com.github.k1mb1.vkr_backend.domain.student_grades.StudentTaskGradeRepository;
import com.github.k1mb1.vkr_backend.domain.students.StudentEntity;
import com.github.k1mb1.vkr_backend.domain.students.StudentRepository;
import com.github.k1mb1.vkr_backend.domain.subjects.responses.*;
import jakarta.persistence.EntityNotFoundException;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GradeSheetService {

    final SubjectRepository subjectRepository;
    final LessonRepository lessonRepository;
    final StudentRepository studentRepository;
    final StudentTaskGradeRepository taskGradeRepository;
    final StudentAttendanceRepository attendanceRepository;

    /**
     * Assembles the full grade-sheet for a subject in exactly 4 DB queries:
     * <ol>
     *   <li>Lessons + tasks (JOIN FETCH)</li>
     *   <li>Students enrolled in the subject</li>
     *   <li>All task grades for the subject</li>
     *   <li>All attendance records for the subject</li>
     * </ol>
     *
     * <p>Group-visibility filtering mirrors the lesson-service rule:
     * a student sees a lesson/task only when {@code lesson.group == null}
     * (lecture, whole cohort) OR {@code lesson.group.id == student.group.id}
     * (practice, student's own subgroup).
     */
    public GradeSheetResponse getGradeSheet(UUID subjectId) {
        if (!subjectRepository.existsById(subjectId)) {
            throw new EntityNotFoundException("Subject not found: " + subjectId);
        }

        // ── 1. Lessons with tasks ──────────────────────────────────────────────
        List<LessonEntity> lessons =
            lessonRepository.findAllWithTasksBySubjectId(subjectId);

        // ── 2. Students ────────────────────────────────────────────────────────
        List<StudentEntity> students =
            studentRepository.findAllBySubjects_Id(subjectId)
                .stream()
                .sorted(Comparator.comparing(StudentEntity::getUsername))
                .toList();

        // ── 3. Task grades – index by (studentId → taskId → entity) ───────────
        Map<UUID, Map<UUID, StudentTaskGradeEntity>> gradeIndex =
            taskGradeRepository.findAllBySubjectId(subjectId)
                .stream()
                .collect(Collectors.groupingBy(
                    g -> g.getStudent().getId(),
                    Collectors.toMap(
                        g -> g.getTask().getId(),
                        g -> g
                    )
                ));

        // ── 4. Attendances – index by (studentId → lessonId → presenceType) ───
        Map<UUID, Map<UUID, com.github.k1mb1.vkr_backend.domain.student_attendances.PresenceType>>
            attendanceIndex = attendanceRepository.findAllBySubjectId(subjectId)
                .stream()
                .collect(Collectors.groupingBy(
                    a -> a.getStudent().getId(),
                    Collectors.toMap(
                        a -> a.getLesson().getId(),
                        a -> a.getPresence()
                    )
                ));

        // ── Build lesson column descriptors ────────────────────────────────────
        List<GradeSheetLessonResponse> lessonResponses = lessons.stream()
            .map(lesson -> new GradeSheetLessonResponse(
                lesson.getId(),
                lesson.getName(),
                lesson.getDateTime(),
                lesson.getType(),
                lesson.getGroup() != null ? lesson.getGroup().getId() : null,
                extractSubgroupNumber(lesson),
                lesson.getDecayFactor(),
                lesson.getTasks().stream()
                    .sorted(Comparator.comparingInt(LessonTaskEntity::getPosition))
                    .map(t -> new GradeSheetTaskResponse(
                        t.getId(),
                        t.getTitle(),
                        t.getMaxPoints(),
                        t.getPosition(),
                        t.getIssuedTaskIndex(),
                        t.getPenaltyMode(),
                        t.getPenaltyStep(),
                        t.isMandatory(),
                        t.getDeadline()
                    ))
                    .toList()
            ))
            .toList();

        // ── Build student row descriptors ──────────────────────────────────────
        List<GradeSheetStudentRow> studentRows = students.stream()
            .map(student -> {
                UUID studentId = student.getId();

                // Only grades for tasks in lessons visible to this student.
                Map<UUID, GradeSheetGradeCell> gradesForStudent = new LinkedHashMap<>();
                for (LessonEntity lesson : lessons) {
                    if (!isLessonVisibleForStudent(lesson, student)) continue;
                    for (LessonTaskEntity task : lesson.getTasks()) {
                        StudentTaskGradeEntity g = gradeIndex
                            .getOrDefault(studentId, Map.of())
                            .get(task.getId());
                        if (g != null) {
                            gradesForStudent.put(
                                task.getId(),
                                new GradeSheetGradeCell(
                                    g.getId(),
                                    g.getValue(),
                                    g.getStatus(),
                                    g.getSubmittedAt()
                                )
                            );
                        }
                    }
                }

                // Attendances only for lessons visible to this student.
                Map<UUID, com.github.k1mb1.vkr_backend.domain.student_attendances.PresenceType>
                    attendancesForStudent = new LinkedHashMap<>();
                for (LessonEntity lesson : lessons) {
                    if (!isLessonVisibleForStudent(lesson, student)) continue;
                    var presence = attendanceIndex
                        .getOrDefault(studentId, Map.of())
                        .get(lesson.getId());
                    if (presence != null) {
                        attendancesForStudent.put(lesson.getId(), presence);
                    }
                }

                return new GradeSheetStudentRow(
                    studentId,
                    student.getUsername(),
                    student.getGroup() != null ? student.getGroup().getId() : null,
                    gradesForStudent,
                    attendancesForStudent
                );
            })
            .toList();

        return new GradeSheetResponse(subjectId, lessonResponses, studentRows);
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    /**
     * A lesson is visible to a student when:
     * - {@code lesson.group == null} — whole-cohort (lecture), OR
     * - {@code lesson.group.id == student.group.id} — student's own subgroup.
     */
    private boolean isLessonVisibleForStudent(LessonEntity lesson, StudentEntity student) {
        if (lesson.getGroup() == null) return true;
        if (student.getGroup() == null) return false;
        return lesson.getGroup().getId().equals(student.getGroup().getId());
    }

    /**
     * Extracts the trailing ordinal from a subgroup name, e.g. "ИСТ-21/2" → 2.
     * Returns {@code null} when the lesson has no group or the name has no {@code /N} suffix.
     */
    private Integer extractSubgroupNumber(LessonEntity lesson) {
        if (lesson.getGroup() == null) return null;
        String name = lesson.getGroup().getName();
        int slash = name.lastIndexOf('/');
        if (slash < 0 || slash == name.length() - 1) return null;
        try {
            return Integer.parseInt(name.substring(slash + 1));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
