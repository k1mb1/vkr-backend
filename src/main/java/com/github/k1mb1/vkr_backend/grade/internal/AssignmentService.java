package com.github.k1mb1.vkr_backend.grade.internal;

import com.github.k1mb1.vkr_backend.grade.AssignmentApi;
import com.github.k1mb1.vkr_backend.grade.domain.Assignment;
import com.github.k1mb1.vkr_backend.grade.web.requests.CreateAssignmentRequest;
import com.github.k1mb1.vkr_backend.grade.web.requests.UpdateAssignmentRequest;
import com.github.k1mb1.vkr_backend.grade.web.responses.AssignmentResponse;
import com.github.k1mb1.vkr_backend.lesson.internal.LessonRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class AssignmentService
    implements AssignmentApi {

    final AssignmentRepository assignmentRepository;
    final AssignmentMapper assignmentMapper;
    final LessonRepository lessonRepository;

    @Transactional
    @Override
    public AssignmentResponse create(CreateAssignmentRequest request) {
        var assignment = Assignment.builder()
            .lesson(lessonRepository.getReferenceById(request.lessonId()))
            .title(request.title())
            .maxScore(request.maxScore())
            .required(request.required())
            .build();
        return assignmentMapper.toResponse(assignmentRepository.save(assignment));
    }

    @Override
    public AssignmentResponse findById(UUID id) {
        return assignmentMapper.toResponse(
            assignmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Assignment not found: " + id))
        );
    }

    @Transactional
    @Override
    public AssignmentResponse update(UUID id, UpdateAssignmentRequest request) {
        var assignment = assignmentRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Assignment not found: " + id));
        assignment.setTitle(request.title());
        assignment.setMaxScore(request.maxScore());
        assignment.setRequired(request.required());
        return assignmentMapper.toResponse(assignmentRepository.save(assignment));
    }

    @Transactional
    @Override
    public void delete(UUID id) {
        if (!assignmentRepository.existsById(id)) {
            throw new EntityNotFoundException("Assignment not found: " + id);
        }
        assignmentRepository.deleteById(id);
    }
}
