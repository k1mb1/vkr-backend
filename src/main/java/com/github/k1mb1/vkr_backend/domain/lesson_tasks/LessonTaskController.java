package com.github.k1mb1.vkr_backend.domain.lesson_tasks;

import com.github.k1mb1.vkr_backend.apis.LessonTaskApi;
import com.github.k1mb1.vkr_backend.domain.lesson_tasks.requests.CreateTaskRequest;
import com.github.k1mb1.vkr_backend.domain.lesson_tasks.requests.UpdateTaskRequest;
import com.github.k1mb1.vkr_backend.domain.lesson_tasks.responses.TaskResponse;
import com.github.k1mb1.vkr_backend.domain.student_grades.StudentTaskGradeService;
import com.github.k1mb1.vkr_backend.domain.student_grades.requests.UpsertTaskGradeRequest;
import com.github.k1mb1.vkr_backend.domain.student_grades.responses.GradeResponse;
import com.github.k1mb1.vkr_backend.domain.student_grades.responses.GradeTableResponse;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Validated
public class LessonTaskController implements LessonTaskApi {

    final LessonTaskService lessonTaskService;
    final StudentTaskGradeService gradeService;

    @Override
    public ResponseEntity<List<TaskResponse>> findAll(UUID lessonId) {
        return ResponseEntity.ok(lessonTaskService.findAllByLesson(lessonId));
    }

    @Override
    public ResponseEntity<TaskResponse> create(UUID lessonId, CreateTaskRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
            lessonTaskService.create(lessonId, request)
        );
    }

    @Override
    public ResponseEntity<TaskResponse> update(
        UUID lessonId,
        UUID taskId,
        UpdateTaskRequest request
    ) {
        return ResponseEntity.ok(lessonTaskService.update(lessonId, taskId, request));
    }

    @Override
    public ResponseEntity<Void> delete(UUID lessonId, UUID taskId) {
        lessonTaskService.delete(lessonId, taskId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<GradeTableResponse> findGrades(UUID lessonId) {
        return ResponseEntity.ok(gradeService.findGradesByLesson(lessonId));
    }

    @Override
    public ResponseEntity<GradeResponse> upsertGrade(
        UUID lessonId,
        UUID taskId,
        UpsertTaskGradeRequest request
    ) {
        return ResponseEntity.ok(gradeService.upsert(lessonId, taskId, request));
    }

    @Override
    public ResponseEntity<List<GradeResponse>> upsertGradesBulk(
        UUID lessonId,
        UUID taskId,
        List<UpsertTaskGradeRequest> requests
    ) {
        return ResponseEntity.ok(gradeService.upsertBulk(lessonId, taskId, requests));
    }
}
