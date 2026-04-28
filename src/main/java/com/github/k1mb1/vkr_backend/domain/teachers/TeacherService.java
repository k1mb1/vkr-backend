package com.github.k1mb1.vkr_backend.domain.teachers;

import com.github.k1mb1.vkr_backend.domain.teachers.requests.CreateOrUpdateTeacherRequest;
import com.github.k1mb1.vkr_backend.domain.teachers.responses.TeacherResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeacherService {

    final TeacherRepository teacherRepository;
    final TeacherMapper teacherMapper;

    @Transactional
    public TeacherResponse createOrUpdate(
        UUID id,
        CreateOrUpdateTeacherRequest request
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
