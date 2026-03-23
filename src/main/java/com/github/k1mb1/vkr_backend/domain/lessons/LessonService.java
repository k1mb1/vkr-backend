package com.github.k1mb1.vkr_backend.domain.lessons;

import com.github.k1mb1.vkr_backend.domain.lessons.requests.CreateLessonRequest;
import com.github.k1mb1.vkr_backend.domain.lessons.requests.CreateLessonsByTypeRequest;
import com.github.k1mb1.vkr_backend.domain.lessons.requests.UpdateLessonRequest;
import com.github.k1mb1.vkr_backend.domain.lessons.responses.LessonResponse;
import com.github.k1mb1.vkr_backend.domain.subjects.SubjectService;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LessonService {

    private final LessonRepository lessonRepository;
    private final SubjectService subjectService;
    private final LessonMapper lessonMapper;

    public Page<LessonResponse> findAll(
        LessonFilter filter,
        Pageable pageable
    ) {
        return lessonRepository
            .findAll(filter.toSpecification(), pageable)
            .map(lessonMapper::toResponse);
    }

    public List<LessonResponse> findAllBySubjectId(UUID subjectId) {
        return lessonRepository
            .findAllBySubject_Id(subjectId)
            .stream()
            .map(lessonMapper::toResponse)
            .toList();
    }

    public List<LessonResponse> findAllByStudentId(UUID studentId) {
        return lessonRepository
            .findAllBySubject_Students_Id(studentId)
            .stream()
            .map(lessonMapper::toResponse)
            .toList();
    }

    public LessonResponse findById(UUID id) {
        return lessonRepository
            .findById(id)
            .map(lessonMapper::toResponse)
            .orElseThrow(() ->
                new EntityNotFoundException("Lesson not found: " + id)
            );
    }

    public LessonEntity findEntityById(UUID id) {
        return lessonRepository
            .findById(id)
            .orElseThrow(() ->
                new EntityNotFoundException("Lesson not found: " + id)
            );
    }

    @Transactional
    public LessonResponse create(CreateLessonRequest request) {
        var subject = subjectService.findEntityById(request.subjectId());
        var entity = lessonMapper
            .toEntity(request)
            .toBuilder()
            .subject(subject)
            .build();
        return lessonMapper.toResponse(lessonRepository.save(entity));
    }

    @Transactional
    public List<LessonResponse> createByType(
        CreateLessonsByTypeRequest request
    ) {
        var subject = subjectService.findEntityById(request.subjectId());
        var result = new java.util.ArrayList<LessonEntity>(
            request.lectureCount() + request.practiceCount()
        );

        for (int i = 0; i < request.lectureCount(); i++) {
            result.add(
                LessonEntity.builder()
                    .name("Лекция " + (i + 1))
                    .type(LessonType.LECTURE)
                    .subject(subject)
                    .build()
            );
        }

        for (int i = 0; i < request.practiceCount(); i++) {
            result.add(
                LessonEntity.builder()
                    .name("Практика " + (i + 1))
                    .type(LessonType.PRACTICE)
                    .subject(subject)
                    .build()
            );
        }

        return lessonRepository
            .saveAll(result)
            .stream()
            .map(lessonMapper::toResponse)
            .toList();
    }

    @Transactional
    public LessonResponse update(UUID id, UpdateLessonRequest request) {
        var entity = lessonRepository
            .findById(id)
            .orElseThrow(() ->
                new EntityNotFoundException("Lesson not found: " + id)
            );
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
