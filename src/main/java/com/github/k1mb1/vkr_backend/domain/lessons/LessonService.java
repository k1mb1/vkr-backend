package com.github.k1mb1.vkr_backend.domain.lessons;

import com.github.k1mb1.vkr_backend.domain.lessons.requests.CreateLessonRequest;
import com.github.k1mb1.vkr_backend.domain.lessons.requests.UpdateLessonRequest;
import com.github.k1mb1.vkr_backend.domain.lessons.responses.LessonResponse;
import com.github.k1mb1.vkr_backend.domain.subjects.SubjectService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LessonService {

    private final LessonRepository lessonRepository;
    private final SubjectService subjectService;
    private final LessonMapper lessonMapper;

    public Page<LessonResponse> findAll(LessonFilter filter, Pageable pageable) {
        return lessonRepository.findAll(filter.toSpecification(), pageable)
                .map(lessonMapper::toResponse);
    }

    public LessonResponse findById(UUID id) {
        return lessonRepository.findById(id)
                .map(lessonMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Lesson not found: " + id));
    }

    public LessonEntity findEntityById(UUID id) {
        return lessonRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Lesson not found: " + id));
    }

    @Transactional
    public LessonResponse create(CreateLessonRequest request) {
        var subject = subjectService.findEntityById(request.subjectId());
        var entity = lessonMapper.toEntity(request).toBuilder().subject(subject).build();
        return lessonMapper.toResponse(lessonRepository.save(entity));
    }

    @Transactional
    public LessonResponse update(UUID id, UpdateLessonRequest request) {
        var entity = lessonRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Lesson not found: " + id));
        if (request.subjectId() != null) {
            var subject = subjectService.findEntityById(request.subjectId());
            entity.toBuilder().subject(subject).build();
        }
        lessonMapper.update(entity, request);
        return lessonMapper.toResponse(lessonRepository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        if (!lessonRepository.existsById(id)) {
            throw new EntityNotFoundException("Lesson not found: " + id);
        }
        lessonRepository.deleteById(id);
    }
}