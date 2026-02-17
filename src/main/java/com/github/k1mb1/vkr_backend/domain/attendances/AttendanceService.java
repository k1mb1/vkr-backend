package com.github.k1mb1.vkr_backend.domain.attendances;

import com.github.k1mb1.vkr_backend.domain.attendances.requests.CreateAttendanceRequest;
import com.github.k1mb1.vkr_backend.domain.attendances.requests.UpdateAttendanceRequest;
import com.github.k1mb1.vkr_backend.domain.attendances.responses.AttendanceResponse;
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
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final LessonService lessonService;
    private final StudentService studentService;
    private final AttendanceMapper attendanceMapper;

    public Page<AttendanceResponse> findAll(AttendanceFilter filter, Pageable pageable) {
        return attendanceRepository.findAll(filter.toSpecification(), pageable)
                .map(attendanceMapper::toResponse);
    }

    public AttendanceResponse findById(UUID id) {
        return attendanceRepository.findById(id)
                .map(attendanceMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Attendance not found: " + id));
    }

    @Transactional
    public AttendanceResponse create(CreateAttendanceRequest request) {
        var lesson = lessonService.findEntityById(request.lessonId());
        var student = studentService.findEntityById(request.studentId());
        var entity = attendanceMapper.toEntity(request).toBuilder().lesson(lesson).student(student).build();
        return attendanceMapper.toResponse(attendanceRepository.save(entity));
    }

    @Transactional
    public AttendanceResponse update(UUID id, UpdateAttendanceRequest request) {
        var entity = attendanceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Attendance not found: " + id));
        attendanceMapper.update(entity, request);
        return attendanceMapper.toResponse(attendanceRepository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        if (!attendanceRepository.existsById(id)) {
            throw new EntityNotFoundException("Attendance not found: " + id);
        }
        attendanceRepository.deleteById(id);
    }
}