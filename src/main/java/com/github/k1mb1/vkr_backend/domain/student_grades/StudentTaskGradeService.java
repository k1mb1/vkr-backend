package com.github.k1mb1.vkr_backend.domain.student_grades;

import com.github.k1mb1.vkr_backend.domain.lesson_tasks.LessonTaskEntity;
import com.github.k1mb1.vkr_backend.domain.lesson_tasks.LessonTaskMapper;
import com.github.k1mb1.vkr_backend.domain.lesson_tasks.LessonTaskRepository;
import com.github.k1mb1.vkr_backend.domain.lessons.LessonFilter;
import com.github.k1mb1.vkr_backend.domain.lessons.PenaltyMode;
import com.github.k1mb1.vkr_backend.domain.student_grades.filters.FindGradesFilter;
import com.github.k1mb1.vkr_backend.domain.student_grades.filters.GradeFilter;
import com.github.k1mb1.vkr_backend.domain.student_grades.requests.UpsertTaskGradeRequest;
import com.github.k1mb1.vkr_backend.domain.student_grades.responses.GradeCellResponse;
import com.github.k1mb1.vkr_backend.domain.student_grades.responses.GradeResponse;
import com.github.k1mb1.vkr_backend.domain.student_grades.responses.GradeTableResponse;
import com.github.k1mb1.vkr_backend.domain.student_grades.responses.GradesTableResponse;
import com.github.k1mb1.vkr_backend.domain.students.StudentEntity;
import com.github.k1mb1.vkr_backend.domain.students.StudentRepository;
import com.github.k1mb1.vkr_backend.domain.students.responses.StudentEntryResponse;
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
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentTaskGradeService {

    final StudentTaskGradeRepository gradeRepository;
    final com.github.k1mb1.vkr_backend.domain.lessons.LessonRepository lessonRepository;
    final LessonTaskRepository taskRepository;
    final LessonTaskMapper taskMapper;
    final StudentRepository studentRepository;
    final SubjectRepository subjectRepository;

    public GradesTableResponse findGradesBySubjectId(UUID subjectId, FindGradesFilter filter) {
        var subject = subjectRepository.findById(subjectId)
            .orElseThrow(() -> new EntityNotFoundException("Subject not found: " + subjectId));

        var lessonFilter = LessonFilter.builder()
            .subjectId(subjectId)
            .lessonType(filter.lessonType())
            .groupId(filter.groupId())
            .build();

        var tasksByLesson = taskRepository.findAllBySubjectId(subjectId).stream()
            .collect(Collectors.groupingBy(t -> t.getLesson().getId()));

        var lessons = lessonRepository.findAll(lessonFilter.toSpecification(), Sort.by(Sort.Direction.ASC, "dateTime"))
            .stream()
            .map(lesson -> new GradesTableResponse.LessonEntryResponse(
                lesson.getId(),
                lesson.getName(),
                lesson.getDateTime(),
                lesson.getType(),
                lesson.getGroup() != null ? lesson.getGroup().getId() : null,
                tasksByLesson.getOrDefault(lesson.getId(), List.of()).stream()
                    .sorted(Comparator.comparingInt(LessonTaskEntity::getPosition))
                    .map(taskMapper::toResponse)
                    .toList()
            ))
            .toList();

        var gradeFilter = new GradeFilter(subjectId, filter.lessonType(), filter.groupId());
        var gradeOrder = Sort.by(Sort.Direction.ASC, "task.lesson.dateTime")
            .and(Sort.by(Sort.Direction.ASC, "task.position"));

        var grades = gradeRepository.findAll(gradeFilter.toSpecification(), gradeOrder)
            .stream()
            .map(this::toCellResponse)
            .toList();

        var students = subject.getStudents().stream()
            .sorted(Comparator.comparing(StudentEntity::getUsername).thenComparing(StudentEntity::getId))
            .map(s -> new StudentEntryResponse(s.getId(), s.getUsername()))
            .toList();

        return new GradesTableResponse(lessons, students, grades);
    }

    public GradeTableResponse findGradesByLesson(UUID lessonId) {
        var rows = gradeRepository.findAllByLessonId(lessonId);

        var tasks = taskRepository.findAllByLesson_IdOrderByPositionAsc(lessonId).stream()
            .map(taskMapper::toResponse)
            .toList();

        var students = rows.stream()
            .map(StudentTaskGradeEntity::getStudent)
            .distinct()
            .sorted(Comparator.comparing(StudentEntity::getUsername).thenComparing(StudentEntity::getId))
            .map(s -> new StudentEntryResponse(s.getId(), s.getUsername()))
            .toList();

        var grades = rows.stream()
            .sorted(Comparator.comparingInt(g -> g.getTask().getPosition()))
            .map(this::toCellResponse)
            .toList();

        return new GradeTableResponse(tasks, students, grades);
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
    public GradeResponse upsert(UUID lessonId, UUID taskId, UpsertTaskGradeRequest request) {
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
    public List<GradeResponse> upsertBulk(
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

    private GradeCellResponse toCellResponse(StudentTaskGradeEntity g) {
        return new GradeCellResponse(
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

    private GradeResponse toResponse(StudentTaskGradeEntity g) {
        return new GradeResponse(
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
}
