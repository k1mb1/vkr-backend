package com.github.k1mb1.vkr_backend.domain.lesson_tasks;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import com.github.k1mb1.vkr_backend.domain.lesson_tasks.requests.CreateTaskRequest;
import com.github.k1mb1.vkr_backend.domain.lesson_tasks.responses.TaskResponse;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = SPRING)
public interface LessonTaskMapper {
    @Mapping(source = "lesson.id", target = "lessonId")
    TaskResponse toResponse(LessonTaskEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "lesson", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    LessonTaskEntity toEntity(CreateTaskRequest request);
}
