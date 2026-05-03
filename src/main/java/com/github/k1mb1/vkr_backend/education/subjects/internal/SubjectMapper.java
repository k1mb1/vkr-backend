package com.github.k1mb1.vkr_backend.education.subjects.internal;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import com.github.k1mb1.vkr_backend.education.subjects.api.requests.CreateSubjectRequest;
import com.github.k1mb1.vkr_backend.education.subjects.api.requests.UpdateSubjectRequest;
import com.github.k1mb1.vkr_backend.education.subjects.api.responses.SubjectResponse;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = SPRING)
public interface SubjectMapper {
    SubjectResponse toResponse(SubjectEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "teacherIds", ignore = true)
    @Mapping(target = "studentIds", ignore = true)
    SubjectEntity toEntity(CreateSubjectRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "teacherIds", ignore = true)
    @Mapping(target = "studentIds", ignore = true)
    void update(@MappingTarget SubjectEntity entity, UpdateSubjectRequest request);
}
