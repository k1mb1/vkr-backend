package com.github.k1mb1.vkr_backend.education.lessons.internal;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import com.github.k1mb1.vkr_backend.education.lessons.api.requests.CreateLessonRequest;
import com.github.k1mb1.vkr_backend.education.lessons.api.requests.UpdateLessonRequest;
import com.github.k1mb1.vkr_backend.education.lessons.api.responses.LessonResponse;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = SPRING)
public interface LessonMapper {

    @Mapping(target = "subgroupNumber", ignore = true)
    LessonResponse toResponse(LessonEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "issuanceMode", expression = "java(request.issuanceMode() != null ? request.issuanceMode() : com.github.k1mb1.vkr_backend.education.lessons.api.IssuanceMode.AUTO)")
    @Mapping(target = "penaltyMode", expression = "java(request.penaltyMode() != null ? request.penaltyMode() : com.github.k1mb1.vkr_backend.education.lessons.api.PenaltyMode.NONE)")
    @Mapping(target = "penaltyStep", expression = "java(request.penaltyStep() != null ? request.penaltyStep() : new java.math.BigDecimal(\"0.25\"))")
    LessonEntity toEntity(CreateLessonRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "subjectId", ignore = true)
    void update(@MappingTarget LessonEntity entity, UpdateLessonRequest request);
}
