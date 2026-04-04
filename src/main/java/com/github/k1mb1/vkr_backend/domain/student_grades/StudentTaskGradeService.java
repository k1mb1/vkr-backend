package com.github.k1mb1.vkr_backend.domain.student_grades;

import com.github.k1mb1.vkr_backend.domain.lessons.LessonTaskRepository;
import com.github.k1mb1.vkr_backend.domain.student_grades.requests.UpsertTaskGradeRequest;
import com.github.k1mb1.vkr_backend.domain.student_grades.responses.StudentTaskGradesResponse;
import com.github.k1mb1.vkr_backend.domain.student_grades.responses.TaskGradeResponse;
import com.github.k1mb1.vkr_backend.domain.students.StudentEntity;
import com.github.k1mb1.vkr_backend.domain.students.StudentRepository;
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

    /**
     * Returns all task grades for a lesson, grouped by student.
     *
     * <p>Only students who have at least one grade row for this lesson are
     * included. Grades within each student are sorted by task position.
     */
    public List<StudentTaskGradesResponse> findGradesByLesson(UUID lessonId) {
        List<StudentTaskGradeEntity> rows = gradeRepository.findAllByLessonId(lessonId);

        Map<UUID, List<StudentTaskGradeEntity>> byStudent = rows.stream()
            .collect(Collectors.groupingBy(g -> g.getStudent().getId()));

        return byStudent.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(e -> {
                StudentEntity student = e.getValue().get(0).getStudent();
                List<TaskGradeResponse> grades = e.getValue().stream()
                    .sorted(Comparator.comparingInt(g -> g.getTask().getPosition()))
                    .map(this::toResponse)
                    .toList();
                return new StudentTaskGradesResponse(
                    student.getId(),
                    student.getUsername(),
                    grades
                );
            })
            .toList();
    }

    /**
     * Upsert semantics: creates a new grade row or updates the existing one
     * for the (task, student) pair.
     */
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

        // Status logic: apply the requested status and auto-set submittedAt.
        SubmissionStatus newStatus = request.status() != null
            ? request.status()
            : grade.getStatus();

        grade.setStatus(newStatus);

        // Auto-set submittedAt when transitioning into SUBMITTED (if not provided explicitly).
        if (newStatus == SubmissionStatus.SUBMITTED && grade.getSubmittedAt() == null) {
            grade.setSubmittedAt(
                request.submittedAt() != null ? request.submittedAt() : Instant.now()
            );
        } else if (request.submittedAt() != null) {
            grade.setSubmittedAt(request.submittedAt());
        }

        // Clear submittedAt when explicitly reset to NOT_SUBMITTED.
        if (newStatus == SubmissionStatus.NOT_SUBMITTED) {
            grade.setSubmittedAt(null);
        }

        return toResponse(gradeRepository.save(grade));
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private TaskGradeResponse toResponse(StudentTaskGradeEntity g) {
        return new TaskGradeResponse(
            g.getId(),
            g.getTask().getId(),
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
