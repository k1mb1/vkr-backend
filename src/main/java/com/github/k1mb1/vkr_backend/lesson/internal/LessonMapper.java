package com.github.k1mb1.vkr_backend.lesson.internal;

import com.github.k1mb1.vkr_backend.lesson.domain.Lesson;
import com.github.k1mb1.vkr_backend.lesson.domain.LessonScope;
import com.github.k1mb1.vkr_backend.lesson.web.requests.UpdateLessonRequest;
import com.github.k1mb1.vkr_backend.lesson.web.responses.LessonResponse;
import com.github.k1mb1.vkr_backend.lesson.web.responses.LessonScopeResponse;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;
import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(componentModel = SPRING)
interface LessonMapper {
    @Mapping(target = "subjectId", source = "subject.id")
    @Mapping(target = "subjectName", source = "subject.name")
    @Mapping(target = "scopes", source = "scopes")
    LessonResponse toResponse(Lesson lesson);

    @Mapping(target = "groupId", source = "group.id")
    @Mapping(target = "groupName", source = "group.name")
    @Mapping(target = "allowedSubgroupId", source = "allowedSubgroup.id")
    @Mapping(target = "allowedSubgroupIndex", source = "allowedSubgroup.index")
    LessonScopeResponse toScopeResponse(LessonScope scope);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "archivedAt", ignore = true)
    @Mapping(target = "subject", ignore = true)
    @Mapping(target = "scopes", ignore = true)
    @Mapping(target = "allGroups", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = IGNORE)
    void updateEntity(
        UpdateLessonRequest request,
        @MappingTarget Lesson lesson
    );
}
