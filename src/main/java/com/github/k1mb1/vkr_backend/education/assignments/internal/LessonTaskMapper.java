package com.github.k1mb1.vkr_backend.education.assignments.internal;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;
import com.github.k1mb1.vkr_backend.education.assignments.api.requests.CreateTaskRequest;
import com.github.k1mb1.vkr_backend.education.assignments.api.requests.UpdateTaskRequest;
import com.github.k1mb1.vkr_backend.education.assignments.api.responses.TaskResponse;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = SPRING)
public interface LessonTaskMapper {
    TaskResponse toResponse(LessonTaskEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "lessonId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    LessonTaskEntity toEntity(CreateTaskRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "lessonId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void update(@MappingTarget LessonTaskEntity entity, UpdateTaskRequest request);
}
