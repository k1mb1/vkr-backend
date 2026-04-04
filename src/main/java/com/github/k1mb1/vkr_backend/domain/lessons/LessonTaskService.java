package com.github.k1mb1.vkr_backend.domain.lessons;

import com.github.k1mb1.vkr_backend.domain.lessons.requests.CreateTaskRequest;
import com.github.k1mb1.vkr_backend.domain.lessons.requests.UpdateTaskRequest;
import com.github.k1mb1.vkr_backend.domain.lessons.responses.TaskResponse;
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
        var lesson = lessonRepository.findById(lessonId)
            .orElseThrow(() -> new EntityNotFoundException("Lesson not found: " + lessonId));

        var entity = taskMapper.toEntity(request)
            .toBuilder()
            .lesson(lesson)
            .build();

        return taskMapper.toResponse(taskRepository.save(entity));
    }

    /**
     * Partial update: only non-null fields in the request are applied.
     *
     * <p>The teacher typically calls this to advance {@code issuedTaskIndex}
     * when a new task is issued so the front-end can recalculate displacement
     * coefficients for older tasks.
     */
    @Transactional
    public TaskResponse update(UUID lessonId, UUID taskId, UpdateTaskRequest request) {
        var entity = taskRepository.findById(taskId)
            .filter(t -> t.getLesson().getId().equals(lessonId))
            .orElseThrow(() -> new EntityNotFoundException(
                "Task " + taskId + " not found in lesson " + lessonId
            ));

        if (request.title()           != null) entity.setTitle(request.title());
        if (request.description()     != null) entity.setDescription(request.description());
        if (request.maxPoints()       != null) entity.setMaxPoints(request.maxPoints());
        if (request.position()        != null) entity.setPosition(request.position());
        if (request.issuedTaskIndex() != null) entity.setIssuedTaskIndex(request.issuedTaskIndex());
        if (request.penaltyMode()     != null) entity.setPenaltyMode(request.penaltyMode());
        if (request.penaltyStep()     != null) entity.setPenaltyStep(request.penaltyStep());

        return taskMapper.toResponse(taskRepository.save(entity));
    }

    @Transactional
    public void delete(UUID lessonId, UUID taskId) {
        var entity = taskRepository.findById(taskId)
            .filter(t -> t.getLesson().getId().equals(lessonId))
            .orElseThrow(() -> new EntityNotFoundException(
                "Task " + taskId + " not found in lesson " + lessonId
            ));
        taskRepository.delete(entity);
    }
}
