package com.github.k1mb1.vkr_backend.domain.lesson_tasks;

import com.github.k1mb1.vkr_backend.domain.lesson_tasks.requests.CreateTaskRequest;
import com.github.k1mb1.vkr_backend.domain.lesson_tasks.requests.UpdateTaskRequest;
import com.github.k1mb1.vkr_backend.domain.lesson_tasks.responses.TaskResponse;
import com.github.k1mb1.vkr_backend.domain.lessons.LessonRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LessonTaskService {

    final LessonTaskRepository taskRepository;
    final LessonRepository lessonRepository;
    final LessonTaskMapper taskMapper;

    public List<TaskResponse> findAllByLesson(UUID lessonId) {
        return taskRepository
            .findAllByLesson_IdOrderByPositionAsc(lessonId)
            .stream()
            .map(taskMapper::toResponse)
            .toList();
    }

    @Transactional
    public TaskResponse create(UUID lessonId, CreateTaskRequest request) {
        var lesson = lessonRepository
            .findById(lessonId)
            .orElseThrow(() ->
                new EntityNotFoundException("Lesson not found: " + lessonId)
            );

        var entity = taskMapper
            .toEntity(request)
            .toBuilder()
            .lesson(lesson)
            .build();

        return taskMapper.toResponse(taskRepository.save(entity));
    }

    /**
     * Partial update: only non-null fields in the request are applied.
     */
    @Transactional
    public TaskResponse update(
        UUID lessonId,
        UUID taskId,
        UpdateTaskRequest request
    ) {
        var entity = taskRepository
            .findByIdAndLesson_Id(taskId, lessonId)
            .orElseThrow(() ->
                new EntityNotFoundException(
                    "Task " + taskId + " not found in lesson " + lessonId
                )
            );

        taskMapper.update(entity, request);
        return taskMapper.toResponse(taskRepository.save(entity));
    }

    @Transactional
    public void delete(UUID lessonId, UUID taskId) {
        var entity = taskRepository
            .findByIdAndLesson_Id(taskId, lessonId)
            .orElseThrow(() ->
                new EntityNotFoundException(
                    "Task " + taskId + " not found in lesson " + lessonId
                )
            );
        taskRepository.delete(entity);
    }
}
