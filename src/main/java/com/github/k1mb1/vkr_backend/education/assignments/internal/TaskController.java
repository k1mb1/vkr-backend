package com.github.k1mb1.vkr_backend.education.assignments.internal;

import com.github.k1mb1.vkr_backend.education.assignments.api.TaskApi;
import com.github.k1mb1.vkr_backend.education.assignments.api.TaskFilter;
import com.github.k1mb1.vkr_backend.education.assignments.api.requests.CreateTaskRequest;
import com.github.k1mb1.vkr_backend.education.assignments.api.requests.UpdateTaskRequest;
import com.github.k1mb1.vkr_backend.education.assignments.api.responses.TaskResponse;
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
class TaskController implements TaskApi {
    final LessonTaskService taskService;

    @Override
    public ResponseEntity<List<TaskResponse>> findAll(UUID lessonId, TaskFilter filter) {
        return new ResponseEntity<>(taskService.findAll(lessonId, filter), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<TaskResponse> create(UUID lessonId, CreateTaskRequest request) {
        return new ResponseEntity<>(taskService.create(lessonId, request), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<TaskResponse> update(UUID lessonId, UUID taskId, UpdateTaskRequest request) {
        return new ResponseEntity<>(taskService.update(lessonId, taskId, request), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> delete(UUID lessonId, UUID taskId) {
        taskService.delete(lessonId, taskId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
