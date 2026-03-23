package com.github.k1mb1.vkr_backend.domain.lessons;

import com.github.k1mb1.vkr_backend.domain.lessons.requests.CreateLessonRequest;
import com.github.k1mb1.vkr_backend.domain.lessons.requests.CreateLessonsByTypeRequest;
import com.github.k1mb1.vkr_backend.domain.lessons.requests.UpdateLessonRequest;
import com.github.k1mb1.vkr_backend.domain.lessons.responses.LessonResponse;
import com.github.k1mb1.vkr_backend.domain.subjects.SubjectService;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LessonService {

    final LessonRepository lessonRepository;
    final SubjectService subjectService;
    final LessonMapper lessonMapper;

    public List<LessonResponse> findAllBySubjectId(UUID subjectId) {
        return lessonRepository
            .findAllBySubject_Id(subjectId)
            .stream()
            .map(lessonMapper::toResponse)
            .toList();
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

        var lectures = IntStream.range(0, request.lectureCount()).mapToObj(i ->
            LessonEntity.builder()
                .name("Лекция " + (i + 1))
                .type(LessonType.LECTURE)
                .subject(subject)
                .build()
        );

        var practices = IntStream.range(0, request.practiceCount()).mapToObj(
            i ->
                LessonEntity.builder()
                    .name("Практика " + (i + 1))
                    .type(LessonType.PRACTICE)
                    .subject(subject)
                    .build()
        );

        return Stream.concat(lectures, practices)
            .collect(
                Collectors.collectingAndThen(
                    Collectors.toList(),
                    lessonRepository::saveAll
                )
            )
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
