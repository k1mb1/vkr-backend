package com.github.k1mb1.vkr_backend.teacher.mapper;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;
import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

import com.github.k1mb1.vkr_backend.teacher.domain.TeacherEntity;
import com.github.k1mb1.vkr_backend.teacher.service.dto.request.CreateOrUpdateTeacherRequest;
import com.github.k1mb1.vkr_backend.teacher.service.dto.response.TeacherResponse;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = SPRING)
public interface TeacherMapper {
    TeacherResponse toResponse(TeacherEntity teacher);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "archivedAt", ignore = true)
    TeacherEntity toEntity(CreateOrUpdateTeacherRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "archivedAt", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = IGNORE)
    void updateEntity(CreateOrUpdateTeacherRequest request, @MappingTarget TeacherEntity teacher);
}
