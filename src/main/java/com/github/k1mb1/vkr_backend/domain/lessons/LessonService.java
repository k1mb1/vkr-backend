package com.github.k1mb1.vkr_backend.domain.lessons;

import com.github.k1mb1.vkr_backend.domain.lessons.requests.*;
import com.github.k1mb1.vkr_backend.domain.lessons.responses.LessonResponse;
import com.github.k1mb1.vkr_backend.domain.student_groups.StudentGroupRepository;
import com.github.k1mb1.vkr_backend.domain.subjects.SubjectService;
import java.util.List;
import java.util.UUID;
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
        var subject = subjectService.getReferenceById(request.subjectId());
        var entity = lessonMapper
            .toEntity(request)
            .toBuilder()
            .subject(subject)
            .build();
        return lessonMapper.toResponse(lessonRepository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        lessonRepository.deleteById(id);
    }
}
