package com.github.k1mb1.vkr_backend.education.structure.internal;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import com.github.k1mb1.vkr_backend.education.structure.api.requests.CreateStudentRequest;
import com.github.k1mb1.vkr_backend.education.structure.api.requests.UpdateStudentRequest;
import com.github.k1mb1.vkr_backend.education.structure.api.responses.StudentResponse;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = SPRING)
public interface StudentMapper {
    @Mapping(source = "group.id", target = "groupId")
    @Mapping(source = "group.name", target = "groupName")
    StudentResponse toResponse(StudentEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "group", ignore = true)
    StudentEntity toEntity(CreateStudentRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "group", ignore = true)
    void update(@MappingTarget StudentEntity entity, UpdateStudentRequest request);
}
