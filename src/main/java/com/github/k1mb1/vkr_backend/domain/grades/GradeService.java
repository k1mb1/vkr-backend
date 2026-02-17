package com.github.k1mb1.vkr_backend.domain.grades;

import com.github.k1mb1.vkr_backend.domain.grades.requests.UpdateGradeRequest;
import com.github.k1mb1.vkr_backend.domain.grades.requests.CreateGradeRequest;
import com.github.k1mb1.vkr_backend.domain.grades.responses.GradeResponse;
import com.github.k1mb1.vkr_backend.domain.lessons.LessonService;
import com.github.k1mb1.vkr_backend.domain.students.StudentService;
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
public class GradeService {

    private final GradeRepository gradeRepository;
    private final LessonService lessonService;
    private final StudentService studentService;
    private final GradeMapper gradeMapper;

    public Page<GradeResponse> findAll(GradeFilter filter, Pageable pageable) {
        return gradeRepository.findAll(filter.toSpecification(), pageable)
                .map(gradeMapper::toResponse);
    }

    public GradeResponse findById(UUID id) {
        return gradeRepository.findById(id)
                .map(gradeMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Grade not found: " + id));
    }

    @Transactional
    public GradeResponse create(CreateGradeRequest request) {
        var lesson = lessonService.findEntityById(request.lessonId());
        var student = studentService.findEntityById(request.studentId());
        var entity = gradeMapper.toEntity(request).toBuilder().lesson(lesson).student(student).build();
        return gradeMapper.toResponse(gradeRepository.save(entity));
    }

    @Transactional
    public GradeResponse update(UUID id, UpdateGradeRequest request) {
        var entity = gradeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Grade not found: " + id));
        gradeMapper.update(entity, request);
        return gradeMapper.toResponse(gradeRepository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        if (!gradeRepository.existsById(id)) {
            throw new EntityNotFoundException("Grade not found: " + id);
        }
        gradeRepository.deleteById(id);
    }
}