package com.github.k1mb1.vkr_backend.domain.grades;


import com.github.k1mb1.vkr_backend.domain.grades.requests.UpdateGradeRequest;
import com.github.k1mb1.vkr_backend.domain.grades.requests.CreateGradeRequest;
import com.github.k1mb1.vkr_backend.domain.grades.responses.GradeResponse;
import org.mapstruct.*;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = SPRING)
public interface GradeMapper {
    @Mapping(source = "lesson.id", target = "lessonId")
    @Mapping(source = "student.id", target = "studentId")
    GradeResponse toResponse(GradeEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "lesson", ignore = true)
    @Mapping(target = "student", ignore = true)
    GradeEntity toEntity(CreateGradeRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "lesson", ignore = true)
    @Mapping(target = "student", ignore = true)
    void update(@MappingTarget GradeEntity entity, UpdateGradeRequest request);
}