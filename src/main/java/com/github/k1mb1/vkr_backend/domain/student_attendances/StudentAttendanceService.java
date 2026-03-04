package com.github.k1mb1.vkr_backend.domain.student_attendances;

import com.github.k1mb1.vkr_backend.domain.student_attendances.requests.BulkCreateAttendanceRequest;
import com.github.k1mb1.vkr_backend.domain.student_attendances.requests.CreateStudentAttendanceRequest;
import com.github.k1mb1.vkr_backend.domain.student_attendances.requests.UpdateStudentAttendanceRequest;
import com.github.k1mb1.vkr_backend.domain.student_attendances.responses.StudentAttendanceResponse;
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
public class StudentAttendanceService {

    private final StudentAttendanceRepository studentAttendanceRepository;
    private final LessonService lessonService;
    private final StudentService studentService;
    private final StudentAttendanceMapper studentAttendanceMapper;

    public Page<StudentAttendanceResponse> findAll(StudentAttendanceFilter filter, Pageable pageable) {
        return studentAttendanceRepository.findAll(filter.toSpecification(), pageable)
                .map(studentAttendanceMapper::toResponse);
    }

    public List<StudentAttendanceResponse> findAllByLessonId(UUID lessonId) {
        return studentAttendanceRepository.findAllByLesson_Id(lessonId)
                .stream()
                .map(studentAttendanceMapper::toResponse)
                .toList();
    }

    public List<StudentAttendanceResponse> findAllByStudentId(UUID studentId) {
        return studentAttendanceRepository.findAllByStudent_Id(studentId)
                .stream()
                .map(studentAttendanceMapper::toResponse)
                .toList();
    }

    public List<StudentAttendanceResponse> findAllBySubjectId(UUID subjectId) {
        return studentAttendanceRepository.findAllByLesson_Subject_Id(subjectId)
                .stream()
                .map(studentAttendanceMapper::toResponse)
                .toList();
    }

    public List<StudentAttendanceResponse> findAllByStudentIdAndSubjectId(UUID studentId, UUID subjectId) {
        return studentAttendanceRepository.findAllByStudent_IdAndLesson_Subject_Id(studentId, subjectId)
                .stream()
                .map(studentAttendanceMapper::toResponse)
                .toList();
    }

    @Transactional
    public List<StudentAttendanceResponse> bulkCreate(UUID lessonId, BulkCreateAttendanceRequest request) {
        var lesson = lessonService.findEntityById(lessonId);
        return request.items().stream()
                .map(item -> {
                    var student = studentService.findEntityById(item.studentId());
                    var entity = StudentAttendanceEntity.builder()
                            .lesson(lesson)
                            .student(student)
                            .presence(item.presence())
                            .note(item.note())
                            .build();
                    return studentAttendanceMapper.toResponse(studentAttendanceRepository.save(entity));
                })
                .toList();
    }

    public StudentAttendanceResponse findById(UUID id) {
        return studentAttendanceRepository.findById(id)
                .map(studentAttendanceMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Attendance not found: " + id));
    }

    @Transactional
    public StudentAttendanceResponse create(CreateStudentAttendanceRequest request) {
        var lesson = lessonService.findEntityById(request.lessonId());
        var student = studentService.findEntityById(request.studentId());
        var entity = studentAttendanceMapper.toEntity(request).toBuilder().lesson(lesson).student(student).build();
        return studentAttendanceMapper.toResponse(studentAttendanceRepository.save(entity));
    }

    @Transactional
    public StudentAttendanceResponse update(UUID id, UpdateStudentAttendanceRequest request) {
        var entity = studentAttendanceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Attendance not found: " + id));
        studentAttendanceMapper.update(entity, request);
        return studentAttendanceMapper.toResponse(studentAttendanceRepository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        if (!studentAttendanceRepository.existsById(id)) {
            throw new EntityNotFoundException("Attendance not found: " + id);
        }
        studentAttendanceRepository.deleteById(id);
    }
}