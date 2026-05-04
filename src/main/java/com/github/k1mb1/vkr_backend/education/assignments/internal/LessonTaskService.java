package com.github.k1mb1.vkr_backend.education.assignments.internal;

import com.github.k1mb1.vkr_backend.education.assignments.api.requests.*;
import com.github.k1mb1.vkr_backend.education.assignments.api.responses.TaskResponse;
import com.github.k1mb1.vkr_backend.education.assignments.api.TaskFilter;
import com.github.k1mb1.vkr_backend.education.lessons.api.LessonQueryFacade;
import jakarta.persistence.EntityNotFoundException;
import java.util.Comparator;
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
    final LessonTaskMapper taskMapper;
    final LessonQueryFacade lessonQueryFacade;

    public List<TaskResponse> findAllByLesson(UUID lessonId) {
        return taskRepository.findAllByLessonIdOrderByPositionAsc(lessonId).stream().map(taskMapper::toResponse).toList();
    }

    public List<TaskResponse> findAll(UUID lessonId, TaskFilter filter) {
        return taskRepository.findAll(filter.toSpecification(lessonId)).stream()
            .sorted(Comparator.comparingInt(LessonTaskEntity::getPosition))
            .map(taskMapper::toResponse)
            .toList();
    }

    @Transactional
    public TaskResponse create(UUID lessonId, CreateTaskRequest request) {
        if (!lessonQueryFacade.existsById(lessonId))
            throw new EntityNotFoundException("Lesson not found: " + lessonId);
        var entity = taskMapper.toEntity(request).toBuilder().lessonId(lessonId).build();
        return taskMapper.toResponse(taskRepository.save(entity));
    }

    @Transactional
    public TaskResponse update(UUID lessonId, UUID taskId, UpdateTaskRequest request) {
        var entity = taskRepository.findByIdAndLessonId(taskId, lessonId)
            .orElseThrow(() -> new EntityNotFoundException("Task " + taskId + " not found in lesson " + lessonId));
        taskMapper.update(entity, request);
        return taskMapper.toResponse(taskRepository.save(entity));
    }

    @Transactional
    public void delete(UUID lessonId, UUID taskId) {
        var entity = taskRepository.findByIdAndLessonId(taskId, lessonId)
            .orElseThrow(() -> new EntityNotFoundException("Task " + taskId + " not found in lesson " + lessonId));
        taskRepository.delete(entity);
    }
}
