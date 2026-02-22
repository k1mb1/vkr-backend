package com.github.k1mb1.vkr_backend.domain.teachers;

import com.github.k1mb1.vkr_backend.domain.subjects.SubjectMapper;
import com.github.k1mb1.vkr_backend.domain.teachers.requests.CreateTeacherRequest;
import com.github.k1mb1.vkr_backend.domain.teachers.requests.UpdateTeacherRequest;
import com.github.k1mb1.vkr_backend.domain.teachers.responses.TeacherDetailsResponse;
import com.github.k1mb1.vkr_backend.domain.teachers.responses.TeacherResponse;
import org.mapstruct.*;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = SPRING, uses = {SubjectMapper.class})
public interface TeacherMapper {

    TeacherResponse toResponse(TeacherEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "subjects", ignore = true)
    TeacherEntity toEntity(CreateTeacherRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "subjects", ignore = true)
    void update(@MappingTarget TeacherEntity entity, UpdateTeacherRequest request);

    @InheritInverseConfiguration(name = "toEntity")
    TeacherDetailsResponse toDetailsResponse(TeacherEntity teacherEntity);
}