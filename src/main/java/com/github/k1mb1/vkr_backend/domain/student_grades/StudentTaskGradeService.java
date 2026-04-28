package com.github.k1mb1.vkr_backend.domain.student_grades;

import com.github.k1mb1.vkr_backend.domain.lesson_tasks.LessonTaskEntity;
import com.github.k1mb1.vkr_backend.domain.lesson_tasks.LessonTaskRepository;
import com.github.k1mb1.vkr_backend.domain.lessons.PenaltyMode;
import com.github.k1mb1.vkr_backend.domain.student_grades.requests.UpsertTaskGradeRequest;
import com.github.k1mb1.vkr_backend.domain.student_grades.responses.StudentTaskGradesResponse;
import com.github.k1mb1.vkr_backend.domain.student_grades.responses.SubjectGradesTableResponse;
import com.github.k1mb1.vkr_backend.domain.student_grades.responses.SubjectGradesTableResponse.SubjectLessonTableEntryResponse;
import com.github.k1mb1.vkr_backend.domain.student_grades.responses.TaskGradeResponse;
import com.github.k1mb1.vkr_backend.domain.students.StudentEntity;
import com.github.k1mb1.vkr_backend.domain.students.StudentRepository;
import com.github.k1mb1.vkr_backend.domain.subjects.SubjectRepository;
import com.github.k1mb1.vkr_backend.domain.subjects.responses.FinalGradeResponse;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
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
public class StudentTaskGradeService {

    final StudentTaskGradeRepository gradeRepository;
    final LessonTaskRepository taskRepository;
    final StudentRepository studentRepository;
    final SubjectRepository subjectRepository;

    public SubjectGradesTableResponse findGradesBySubjectId(UUID subjectId) {
        var subject = subjectRepository.findById(subjectId)
            .orElseThrow(() -> new EntityNotFoundException("Subject not found: " + subjectId));

        var rowsByStudent = gradeRepository.findAllBySubjectId(subjectId).stream()
            .collect(Collectors.groupingBy(g -> g.getStudent().getId()));

        var gradeOrder = Comparator
            .comparing(
                (StudentTaskGradeEntity g) -> g.getTask().getLesson().getDateTime(),
                Comparator.nullsLast(Comparator.naturalOrder())
            )
            .thenComparingInt(g -> g.getTask().getPosition());

        var students = subject.getStudents().stream()
            .sorted(
                Comparator.comparing(StudentEntity::getUsername)
                    .thenComparing(StudentEntity::getId)
            )
            .map(student -> new StudentTaskGradesResponse(
                student.getId(),
                student.getUsername(),
                rowsByStudent.getOrDefault(student.getId(), List.of()).stream()
                    .sorted(gradeOrder)
                    .map(this::toResponse)
                    .toList()
            ))
            .toList();

        var lessons = subject.getLessons().stream()
            .sorted(
                Comparator.comparing(
                    StudentTaskGradeService::lessonDateTime,
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

        return new SubjectGradesTableResponse(lessons, students);
    }

    public List<StudentTaskGradesResponse> findGradesByLesson(UUID lessonId) {
        return groupByStudent(
            gradeRepository.findAllByLessonId(lessonId),
            Comparator.comparingInt(g -> g.getTask().getPosition())
        );
    }

    /**
     * Computes the aggregated final grade for every student enrolled in a subject.
     *
     * <p>For each (student, task) pair:
     * <ul>
     *   <li>Displacement {@code d = max(0, lesson.issuedTaskIndex - task.position)}</li>
     *   <li>Coefficient is derived from the lesson's {@code penaltyMode} and {@code penaltyStep}</li>
     *   <li>Mandatory tasks with no grade count as 0 earned points</li>
     *   <li>Bonus tasks (isMandatory=false) with no grade are skipped entirely</li>
     * </ul>
     */
    public List<FinalGradeResponse> computeFinalGrades(UUID subjectId) {
        var subject = subjectRepository.findById(subjectId)
            .orElseThrow(() -> new EntityNotFoundException("Subject not found: " + subjectId));

        var tasks = taskRepository.findAllBySubjectId(subjectId);

        var gradesByStudentAndTask = gradeRepository.findAllBySubjectId(subjectId)
            .stream()
            .collect(Collectors.groupingBy(
                g -> g.getStudent().getId(),
                Collectors.toMap(g -> g.getTask().getId(), g -> g)
            ));

        return subject.getStudents().stream()
            .sorted(Comparator.comparing(StudentEntity::getUsername))
            .map(student -> buildFinalGrade(student, tasks, gradesByStudentAndTask))
            .toList();
    }

    @Transactional
    public TaskGradeResponse upsert(UUID lessonId, UUID taskId, UpsertTaskGradeRequest request) {
        var task = taskRepository.findById(taskId)
            .filter(t -> t.getLesson().getId().equals(lessonId))
            .orElseThrow(() -> new EntityNotFoundException(
                "Task " + taskId + " not found in lesson " + lessonId
            ));

        var student = studentRepository.findById(request.studentId())
            .orElseThrow(() -> new EntityNotFoundException(
                "Student not found: " + request.studentId()
            ));

        var grade = gradeRepository
            .findByTask_IdAndStudent_Id(taskId, request.studentId())
            .orElseGet(() -> StudentTaskGradeEntity.builder()
                .task(task)
                .student(student)
                .build()
            );

        if (request.value()   != null) grade.setValue(request.value());
        if (request.comment() != null) grade.setComment(request.comment());

        SubmissionStatus newStatus = request.status() != null
            ? request.status()
            : grade.getStatus();

        grade.setStatus(newStatus);

        if (newStatus == SubmissionStatus.SUBMITTED && grade.getSubmittedAt() == null) {
            grade.setSubmittedAt(
                request.submittedAt() != null ? request.submittedAt() : Instant.now()
            );
        } else if (request.submittedAt() != null) {
            grade.setSubmittedAt(request.submittedAt());
        }

        if (newStatus == SubmissionStatus.NOT_SUBMITTED) {
            grade.setSubmittedAt(null);
        }

        return toResponse(gradeRepository.save(grade));
    }

    @Transactional
    public List<TaskGradeResponse> upsertBulk(
        UUID lessonId,
        UUID taskId,
        List<UpsertTaskGradeRequest> requests
    ) {
        return requests.stream()
            .map(req -> upsert(lessonId, taskId, req))
            .toList();
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private FinalGradeResponse buildFinalGrade(
        StudentEntity student,
        List<LessonTaskEntity> tasks,
        Map<UUID, Map<UUID, StudentTaskGradeEntity>> gradesByStudentAndTask
    ) {
        var studentGrades = gradesByStudentAndTask.getOrDefault(student.getId(), Map.of());

        double earned = 0;
        double max = 0;

        for (var task : tasks) {
            var lesson = task.getLesson();
            int d = Math.max(0, lesson.getIssuedTaskIndex() - task.getPosition());
            double coeff = coefficient(lesson.getPenaltyMode(), lesson.getPenaltyStep().doubleValue(), d);

            if (task.isMandatory()) {
                max += task.getMaxPoints() * coeff;
            }

            var grade = studentGrades.get(task.getId());
            if (grade != null && grade.getValue() != null) {
                earned += grade.getValue() * coeff;
            }
        }

        Double percentage = max > 0 ? earned / max * 100.0 : null;
        return new FinalGradeResponse(student.getId(), student.getUsername(), earned, max, percentage);
    }

    private double coefficient(PenaltyMode mode, double step, int d) {
        if (d <= 0 || mode == PenaltyMode.NONE) return 1.0;
        return switch (mode) {
            case SUBTRACT -> Math.max(0.0, 1.0 - step * d);
            case MULTIPLY -> Math.pow(step, d);
            case NONE     -> 1.0;
        };
    }

    private List<StudentTaskGradesResponse> groupByStudent(
        List<StudentTaskGradeEntity> rows,
        Comparator<StudentTaskGradeEntity> order
    ) {
        Map<UUID, List<StudentTaskGradeEntity>> byStudent = rows.stream()
            .collect(Collectors.groupingBy(g -> g.getStudent().getId()));

        return byStudent.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(e -> {
                StudentEntity student = e.getValue().get(0).getStudent();
                List<TaskGradeResponse> grades = e.getValue().stream()
                    .sorted(order)
                    .map(this::toResponse)
                    .toList();
                return new StudentTaskGradesResponse(student.getId(), student.getUsername(), grades);
            })
            .toList();
    }

    private TaskGradeResponse toResponse(StudentTaskGradeEntity g) {
        return new TaskGradeResponse(
            g.getId(),
            g.getTask().getId(),
            g.getTask().getLesson().getId(),
            g.getStudent().getId(),
            g.getValue(),
            g.getComment(),
            g.getStatus(),
            g.getSubmittedAt(),
            g.getCreatedAt(),
            g.getUpdatedAt()
        );
    }

    private static java.time.OffsetDateTime lessonDateTime(
        com.github.k1mb1.vkr_backend.domain.lessons.LessonEntity lesson
    ) {
        return lesson.getDateTime();
    }
}
