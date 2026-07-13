package com.github.k1mb1.vkr_backend.teacher.service;

import com.github.k1mb1.vkr_backend.teacher.domain.TeacherEntity;
import com.github.k1mb1.vkr_backend.teacher.mapper.TeacherMapper;
import com.github.k1mb1.vkr_backend.teacher.repository.TeacherRepository;
import com.github.k1mb1.vkr_backend.teacher.service.dto.filter.TeacherFilter;
import com.github.k1mb1.vkr_backend.teacher.service.dto.request.CreateOrUpdateTeacherRequest;
import com.github.k1mb1.vkr_backend.teacher.service.dto.response.TeacherResponse;
import com.github.k1mb1.vkr_backend.teacher.specification.TeacherSpecification;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class TeacherService {

    final TeacherRepository teacherRepository;

    final TeacherMapper teacherMapper;

    @Transactional
    public TeacherResponse createOrUpdateTeacher(UUID id, CreateOrUpdateTeacherRequest request) {
        var teacher = teacherRepository
                .findById(id)
                .orElseGet(() -> TeacherEntity.builder().id(id).build());

        teacherMapper.updateEntity(request, teacher);
        return teacherMapper.toResponse(teacherRepository.save(teacher));
    }

    public Page<TeacherResponse> getPage(TeacherFilter filter, Pageable pageable) {
        return teacherRepository
                .findAll(new TeacherSpecification(filter).toSpecification(), pageable)
                .map(teacherMapper::toResponse);
    }
}
