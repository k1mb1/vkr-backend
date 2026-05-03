package com.github.k1mb1.vkr_backend.education.structure.internal;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import com.github.k1mb1.vkr_backend.education.structure.api.requests.CreateOrUpdateTeacherRequest;
import com.github.k1mb1.vkr_backend.education.structure.api.responses.TeacherResponse;
import java.util.UUID;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = SPRING)
public interface TeacherMapper {
    TeacherResponse toResponse(TeacherEntity entity);

    @Mapping(target = "id", source = "id")
    TeacherEntity toEntity(UUID id, CreateOrUpdateTeacherRequest request);

    @Mapping(target = "id", ignore = true)
    void update(@MappingTarget TeacherEntity entity, CreateOrUpdateTeacherRequest request);
}
