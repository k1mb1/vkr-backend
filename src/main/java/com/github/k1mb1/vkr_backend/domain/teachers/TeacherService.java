package com.github.k1mb1.vkr_backend.domain.teachers;

import com.github.k1mb1.vkr_backend.domain.teachers.requests.UpdateTeacherRequest;
import com.github.k1mb1.vkr_backend.domain.teachers.responses.TeacherDetailsResponse;
import com.github.k1mb1.vkr_backend.domain.teachers.responses.TeacherResponse;
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
public class TeacherService {

    final TeacherRepository teacherRepository;
    final TeacherMapper teacherMapper;

    public Page<TeacherResponse> findAll(
        TeacherFilter filter,
        Pageable pageable
    ) {
        return teacherRepository
            .findAll(filter.toSpecification(), pageable)
            .map(teacherMapper::toResponse);
    }

    public List<TeacherResponse> findAllBySubjectId(UUID subjectId) {
        return teacherRepository
            .findAllBySubjects_Id(subjectId)
            .stream()
            .map(teacherMapper::toResponse)
            .toList();
    }

    public TeacherDetailsResponse findById(UUID id) {
        return teacherRepository
            .findWithSubjectsById(id)
            .map(teacherMapper::toDetailsResponse)
            .orElseThrow(() ->
                new EntityNotFoundException("Teacher not found: " + id)
            );
    }

    @Transactional
    public TeacherResponse createOrUpdate(
        UUID id,
        UpdateTeacherRequest request
    ) {
        return teacherRepository
            .findById(id)
            .map(existing -> {
                teacherMapper.update(existing, request);
                return teacherMapper.toResponse(
                    teacherRepository.save(existing)
                );
            })
            .orElseGet(() ->
                teacherMapper.toResponse(
                    teacherRepository.save(teacherMapper.toEntity(id, request))
                )
            );
    }
}
