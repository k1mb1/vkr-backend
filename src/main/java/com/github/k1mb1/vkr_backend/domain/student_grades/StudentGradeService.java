package com.github.k1mb1.vkr_backend.domain.student_grades;

import com.github.k1mb1.vkr_backend.domain.student_grades.requests.UpdateStudentGradeRequest;
import com.github.k1mb1.vkr_backend.domain.student_grades.requests.BulkCreateGradeRequest;
import com.github.k1mb1.vkr_backend.domain.student_grades.requests.CreateStudentGradeRequest;
import com.github.k1mb1.vkr_backend.domain.student_grades.responses.StudentGradeResponse;
import com.github.k1mb1.vkr_backend.domain.lessons.LessonService;
import com.github.k1mb1.vkr_backend.domain.students.StudentService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentGradeService {

    private final StudentGradeRepository studentGradeRepository;
    private final LessonService lessonService;
    private final StudentService studentService;
    private final StudentGradeMapper studentGradeMapper;

    public Page<StudentGradeResponse> findAll(StudentGradeFilter filter, Pageable pageable) {
        return studentGradeRepository.findAll(filter.toSpecification(), pageable)
                .map(studentGradeMapper::toResponse);
    }

    public List<StudentGradeResponse> findAllByLessonId(UUID lessonId) {
        return studentGradeRepository.findAllByLesson_Id(lessonId)
                .stream()
                .map(studentGradeMapper::toResponse)
                .toList();
    }

    public List<StudentGradeResponse> findAllByStudentId(UUID studentId) {
        return studentGradeRepository.findAllByStudent_Id(studentId)
                .stream()
                .map(studentGradeMapper::toResponse)
                .toList();
    }

    public List<StudentGradeResponse> findAllBySubjectId(UUID subjectId) {
        return studentGradeRepository.findAllByLesson_Subject_Id(subjectId)
                .stream()
                .map(studentGradeMapper::toResponse)
                .toList();
    }

    public List<StudentGradeResponse> findAllByStudentIdAndSubjectId(UUID studentId, UUID subjectId) {
        return studentGradeRepository.findAllByStudent_IdAndLesson_Subject_Id(studentId, subjectId)
                .stream()
                .map(studentGradeMapper::toResponse)
                .toList();
    }

    @Transactional
    public List<StudentGradeResponse> bulkCreate(UUID lessonId, BulkCreateGradeRequest request) {
        var lesson = lessonService.findEntityById(lessonId);
        return request.items().stream()
                .map(item -> {
                    var student = studentService.findEntityById(item.studentId());
                    var entity = StudentGradeEntity.builder()
                            .lesson(lesson)
                            .student(student)
                            .value(item.value())
                            .comment(item.comment())
                            .build();
                    return studentGradeMapper.toResponse(studentGradeRepository.save(entity));
                })
                .toList();
    }

    public StudentGradeResponse findById(UUID id) {
        return studentGradeRepository.findById(id)
                .map(studentGradeMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Grade not found: " + id));
    }

    @Transactional
    public StudentGradeResponse create(CreateStudentGradeRequest request) {
        var lesson = lessonService.findEntityById(request.lessonId());
        var student = studentService.findEntityById(request.studentId());
        var entity = studentGradeMapper.toEntity(request).toBuilder().lesson(lesson).student(student).build();
        return studentGradeMapper.toResponse(studentGradeRepository.save(entity));
    }

    @Transactional
    public StudentGradeResponse update(UUID id, UpdateStudentGradeRequest request) {
        var entity = studentGradeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Grade not found: " + id));
        studentGradeMapper.update(entity, request);
        return studentGradeMapper.toResponse(studentGradeRepository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        if (!studentGradeRepository.existsById(id)) {
            throw new EntityNotFoundException("Grade not found: " + id);
        }
        studentGradeRepository.deleteById(id);
    }
}