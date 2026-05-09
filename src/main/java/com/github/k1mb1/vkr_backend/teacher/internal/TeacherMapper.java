package com.github.k1mb1.vkr_backend.teacher.internal;

import com.github.k1mb1.vkr_backend.teacher.TeacherResponse;
import com.github.k1mb1.vkr_backend.teacher.domain.Teacher;
import com.github.k1mb1.vkr_backend.teacher.web.requests.CreateOrUpdateTeacherRequest;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TeacherMapper {

    TeacherResponse toResponse(Teacher teacher);

    Teacher toEntity(CreateOrUpdateTeacherRequest request);

    void updateEntity(CreateOrUpdateTeacherRequest request, @MappingTarget Teacher teacher);
}
