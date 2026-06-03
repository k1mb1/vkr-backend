package com.github.k1mb1.vkr_backend.teacher.internal;

import com.github.k1mb1.vkr_backend.common.GeneratedMapper;
import org.mapstruct.AnnotateWith;

import com.github.k1mb1.vkr_backend.teacher.domain.Teacher;
import com.github.k1mb1.vkr_backend.teacher.web.requests.CreateOrUpdateTeacherRequest;
import com.github.k1mb1.vkr_backend.teacher.web.response.TeacherResponse;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;
import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@AnnotateWith(GeneratedMapper.class)
@Mapper(componentModel = SPRING)
interface TeacherMapper {
    TeacherResponse toResponse(Teacher teacher);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "archivedAt", ignore = true)
    Teacher toEntity(CreateOrUpdateTeacherRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "archivedAt", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = IGNORE)
    void updateEntity(
        CreateOrUpdateTeacherRequest request,
        @MappingTarget Teacher teacher
    );
}
