package com.github.k1mb1.vkr_backend.education.assignments.internal;

import com.github.k1mb1.vkr_backend.education.assignments.api.GradeFilter;
import com.github.k1mb1.vkr_backend.education.assignments.api.SubmissionStatus;
import com.github.k1mb1.vkr_backend.education.assignments.api.requests.UpsertTaskGradeRequest;
import com.github.k1mb1.vkr_backend.education.assignments.api.responses.*;
import com.github.k1mb1.vkr_backend.education.lessons.api.LessonInfo;
import com.github.k1mb1.vkr_backend.education.lessons.api.LessonQueryFacade;
import com.github.k1mb1.vkr_backend.education.lessons.api.PenaltyMode;
import com.github.k1mb1.vkr_backend.education.structure.api.StructureQueryFacade;
import com.github.k1mb1.vkr_backend.education.structure.api.responses.StudentEntryResponse;
import com.github.k1mb1.vkr_backend.education.subjects.api.SubjectQueryFacade;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentTaskGradeService {
    final StudentTaskGradeRepository gradeRepository;
    final LessonTaskRepository taskRepository;
    final LessonTaskMapper taskMapper;
    final LessonQueryFacade lessonQueryFacade;
    final SubjectQueryFacade subjectQueryFacade;
    final StructureQueryFacade structureQueryFacade;

    public GradeMatrixResponse findGradesBySubjectId(UUID subjectId, FindGradesFilter filter) {
        if (!subjectQueryFacade.existsById(subjectId))
            throw new EntityNotFoundException("Subject not found: " + subjectId);

        var allLessons = filter.groupId() != null
            ? lessonQueryFacade.findBySubjectIdAndGroupId(subjectId, filter.groupId())
            : filter.lessonType() != null
                ? lessonQueryFacade.findBySubjectIdAndType(subjectId, filter.lessonType())
                : lessonQueryFacade.findBySubjectId(subjectId);

        var lessonIds = allLessons.stream().map(LessonInfo::id).toList();
        var tasksByLesson = taskRepository.findAllByLessonIdIn(lessonIds).stream()
            .collect(Collectors.groupingBy(LessonTaskEntity::getLessonId));

        var lessonEntries = allLessons.stream().map(l ->
            new GradeMatrixResponse.LessonEntry(l.id(), l.name(), l.dateTime(), l.type(), l.groupId(),
                tasksByLesson.getOrDefault(l.id(), List.of()).stream()
                    .sorted(Comparator.comparingInt(LessonTaskEntity::getPosition))
                    .map(taskMapper::toResponse).toList())
        ).toList();

        var taskIds = tasksByLesson.values().stream().flatMap(Collection::stream).map(t -> t.getId()).toList();
        var grades = gradeRepository.findAllByTask_IdIn(taskIds).stream()
            .map(this::toCellResponse).toList();

        var studentIds = subjectQueryFacade.findStudentIdsBySubjectId(subjectId);
        var students = structureQueryFacade.findStudentsByIds(studentIds).stream()
            .sorted(Comparator.comparing(s -> s.username()))
            .map(s -> new StudentEntryResponse(s.id(), s.username()))
            .toList();

        return new GradeMatrixResponse(lessonEntries, students, grades);
    }

    public GradeTableResponse findGradesByLesson(UUID lessonId) {
        var grades = gradeRepository.findAllByLessonId(lessonId);
        var tasks = taskRepository.findAllByLessonIdOrderByPositionAsc(lessonId).stream().map(taskMapper::toResponse).toList();
        var studentIds = grades.stream().map(StudentTaskGradeEntity::getStudentId).distinct().toList();
        var students = structureQueryFacade.findStudentsByIds(studentIds).stream()
            .sorted(Comparator.comparing(s -> s.username()))
            .map(s -> new StudentEntryResponse(s.id(), s.username()))
            .toList();
        return new GradeTableResponse(tasks, students, grades.stream().map(this::toCellResponse).toList());
    }

    public List<FinalGradeResponse> computeFinalGrades(UUID subjectId) {
        if (!subjectQueryFacade.existsById(subjectId))
            throw new EntityNotFoundException("Subject not found: " + subjectId);

        var lessons = lessonQueryFacade.findBySubjectId(subjectId);
        var lessonIds = lessons.stream().map(LessonInfo::id).toList();
        var lessonMap = lessons.stream().collect(Collectors.toMap(LessonInfo::id, l -> l));
        var tasks = taskRepository.findAllByLessonIdIn(lessonIds);

        var gradesByStudent = gradeRepository.findAllByTask_IdIn(tasks.stream().map(t -> t.getId()).toList())
            .stream().collect(Collectors.groupingBy(StudentTaskGradeEntity::getStudentId,
                Collectors.toMap(g -> g.getTask().getId(), g -> g)));

        var studentIds = subjectQueryFacade.findStudentIdsBySubjectId(subjectId);
        return structureQueryFacade.findStudentsByIds(studentIds).stream()
            .sorted(Comparator.comparing(s -> s.username()))
            .map(student -> {
                var sg = gradesByStudent.getOrDefault(student.id(), Map.of());
                double earned = 0, max = 0;
                for (var task : tasks) {
                    var lesson = lessonMap.get(task.getLessonId());
                    if (lesson == null) continue;
                    int d = Math.max(0, lesson.issuedTaskIndex() - task.getPosition());
                    double coeff = coefficient(lesson.penaltyMode(), lesson.penaltyStep().doubleValue(), d);
                    if (task.isMandatory()) max += task.getMaxPoints() * coeff;
                    var g = sg.get(task.getId());
                    if (g != null && g.getValue() != null) earned += g.getValue() * coeff;
                }
                Double pct = max > 0 ? earned / max * 100.0 : null;
                return new FinalGradeResponse(student.id(), student.username(), earned, max, pct);
            }).toList();
    }

    public Page<GradeResponse> findGrades(UUID taskId, GradeFilter filter, Pageable pageable) {
        return gradeRepository.findAll(filter.toSpecification(taskId), pageable)
            .map(g -> new GradeResponse(g.getId(), g.getTask().getId(), g.getTask().getLessonId(),
                g.getStudentId(), g.getValue(), g.getComment(), g.getStatus(), g.getSubmittedAt(),
                g.getCreatedAt(), g.getUpdatedAt()));
    }

    @Transactional
    public GradeResponse upsert(UUID lessonId, UUID taskId, UpsertTaskGradeRequest request) {
        var task = taskRepository.findByIdAndLessonId(taskId, lessonId)
            .orElseThrow(() -> new EntityNotFoundException("Task " + taskId + " not found in lesson " + lessonId));
        if (!structureQueryFacade.studentExists(request.studentId()))
            throw new EntityNotFoundException("Student not found: " + request.studentId());

        var grade = gradeRepository.findByTask_IdAndStudentId(taskId, request.studentId())
            .orElseGet(() -> StudentTaskGradeEntity.builder().task(task).studentId(request.studentId()).build());

        if (request.value() != null) grade.setValue(request.value());
        if (request.comment() != null) grade.setComment(request.comment());
        SubmissionStatus newStatus = request.status() != null ? request.status() : grade.getStatus();
        grade.setStatus(newStatus);
        if (newStatus == SubmissionStatus.SUBMITTED && grade.getSubmittedAt() == null)
            grade.setSubmittedAt(request.submittedAt() != null ? request.submittedAt() : Instant.now());
        else if (request.submittedAt() != null) grade.setSubmittedAt(request.submittedAt());
        if (newStatus == SubmissionStatus.NOT_SUBMITTED) grade.setSubmittedAt(null);

        var saved = gradeRepository.save(grade);
        return new GradeResponse(saved.getId(), saved.getTask().getId(), saved.getTask().getLessonId(),
            saved.getStudentId(), saved.getValue(), saved.getComment(), saved.getStatus(), saved.getSubmittedAt(),
            saved.getCreatedAt(), saved.getUpdatedAt());
    }

    @Transactional
    public List<GradeResponse> upsertBulk(UUID lessonId, UUID taskId, List<UpsertTaskGradeRequest> requests) {
        return requests.stream().map(r -> upsert(lessonId, taskId, r)).toList();
    }

    private GradeCellResponse toCellResponse(StudentTaskGradeEntity g) {
        return new GradeCellResponse(g.getId(), g.getTask().getId(), g.getTask().getLessonId(),
            g.getStudentId(), g.getValue(), g.getComment(), g.getStatus(), g.getSubmittedAt(),
            g.getCreatedAt(), g.getUpdatedAt());
    }

    private double coefficient(PenaltyMode mode, double step, int d) {
        if (d <= 0 || mode == PenaltyMode.NONE) return 1.0;
        return switch (mode) {
            case SUBTRACT -> Math.max(0.0, 1.0 - step * d);
            case MULTIPLY -> Math.pow(step, d);
            case NONE -> 1.0;
        };
    }
}
