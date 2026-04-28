package com.github.k1mb1.vkr_backend.domain.lessons;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import com.github.k1mb1.vkr_backend.domain.lessons.requests.CreateLessonRequest;
import com.github.k1mb1.vkr_backend.domain.lessons.requests.UpdateLessonRequest;
import com.github.k1mb1.vkr_backend.domain.lessons.responses.LessonResponse;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = SPRING)
public interface LessonMapper {
    @Mapping(source = "subject.id", target = "subjectId")
    @Mapping(source = "group.id", target = "groupId")
    @Mapping(
        target = "subgroupNumber",
        expression = "java(extractSubgroupNumber(entity))"
    )
    LessonResponse toResponse(LessonEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "subject", ignore = true)
    @Mapping(target = "tasks", ignore = true)
    @Mapping(target = "issuanceMode", expression = "java(request.issuanceMode() != null ? request.issuanceMode() : com.github.k1mb1.vkr_backend.domain.lessons.IssuanceMode.AUTO)")
    @Mapping(target = "penaltyMode", expression = "java(request.penaltyMode() != null ? request.penaltyMode() : com.github.k1mb1.vkr_backend.domain.lessons.PenaltyMode.NONE)")
    @Mapping(target = "penaltyStep", expression = "java(request.penaltyStep() != null ? request.penaltyStep() : new java.math.BigDecimal(\"0.25\"))")
    LessonEntity toEntity(CreateLessonRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "subject", ignore = true)
    @Mapping(target = "tasks", ignore = true)
    @Mapping(target = "group", ignore = true)
    @Mapping(target = "attendances", ignore = true)
    void update(@MappingTarget LessonEntity entity, UpdateLessonRequest request);

    /**
     * Extracts the subgroup ordinal from the group name.
     *
     * <p>Convention: subgroup names follow the pattern {@code <parentName>/<number>},
     * e.g. {@code "ИСТ-21/1"} → {@code 1}. Returns {@code null} when the lesson
     * has no group or the group name does not end with {@code /<integer>}.
     */
    default Integer extractSubgroupNumber(LessonEntity entity) {
        if (entity.getGroup() == null) return null;
        String name = entity.getGroup().getName();
        int slash = name.lastIndexOf('/');
        if (slash < 0 || slash == name.length() - 1) return null;
        try {
            return Integer.parseInt(name.substring(slash + 1));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
