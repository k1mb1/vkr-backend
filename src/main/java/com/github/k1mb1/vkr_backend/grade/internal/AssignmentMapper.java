package com.github.k1mb1.vkr_backend.grade.internal;

import com.github.k1mb1.vkr_backend.grade.domain.Assignment;
import com.github.k1mb1.vkr_backend.grade.web.responses.AssignmentResponse;
import com.github.k1mb1.vkr_backend.grade.web.responses.GradeTableColumn;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
interface AssignmentMapper {

    @Mapping(target = "lessonId", source = "lesson.id")
    AssignmentResponse toResponse(Assignment assignment);

    @Mapping(target = "lessonId", source = "lesson.id")
    @Mapping(target = "lessonDate", source = "lesson.startedAt")
    @Mapping(target = "lessonType", source = "lesson.type")
    GradeTableColumn toColumn(Assignment assignment);
}
