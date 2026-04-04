package com.github.k1mb1.vkr_backend.domain.lessons;

import com.github.k1mb1.vkr_backend.apis.LessonTaskApi;
import com.github.k1mb1.vkr_backend.domain.lessons.requests.CreateTaskRequest;
import com.github.k1mb1.vkr_backend.domain.lessons.requests.UpdateTaskRequest;
import com.github.k1mb1.vkr_backend.domain.lessons.responses.TaskResponse;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class LessonTaskController implements LessonTaskApi {

    final LessonTaskService lessonTaskService;

    @Override
    public ResponseEntity<List<TaskResponse>> findAll(UUID lessonId) {
        return ResponseEntity.ok(lessonTaskService.findAllByLesson(lessonId));
    }

    @Override
    public ResponseEntity<TaskResponse> create(UUID lessonId, CreateTaskRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(lessonTaskService.create(lessonId, request));
    }

    @Override
    public ResponseEntity<TaskResponse> update(UUID lessonId, UUID taskId, UpdateTaskRequest request) {
        return ResponseEntity.ok(lessonTaskService.update(lessonId, taskId, request));
    }

    @Override
    public ResponseEntity<Void> delete(UUID lessonId, UUID taskId) {
        lessonTaskService.delete(lessonId, taskId);
        return ResponseEntity.noContent().build();
    }
}
