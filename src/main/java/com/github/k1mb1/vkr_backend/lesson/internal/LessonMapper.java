package com.github.k1mb1.vkr_backend.lesson.internal;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;
import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

import com.github.k1mb1.vkr_backend.grading.web.responses.AssignmentResponse;
import com.github.k1mb1.vkr_backend.lesson.domain.Lesson;
import com.github.k1mb1.vkr_backend.lesson.domain.LessonScope;
import com.github.k1mb1.vkr_backend.lesson.web.requests.UpdateLessonHeaderRequest;
import com.github.k1mb1.vkr_backend.lesson.web.responses.LessonResponse;
import com.github.k1mb1.vkr_backend.lesson.web.responses.LessonScopeResponse;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = SPRING)
interface LessonMapper {
    @Mapping(target = "id", source = "lesson.id")
    @Mapping(target = "subjectId", source = "lesson.subject.id")
    @Mapping(target = "subjectName", source = "lesson.subject.name")
    @Mapping(target = "type", source = "lesson.type")
    @Mapping(target = "orderIndex", source = "lesson.orderIndex")
    @Mapping(target = "topic", source = "lesson.topic")
    @Mapping(target = "active", source = "lesson.active")
    @Mapping(target = "createdAt", source = "lesson.createdAt")
    @Mapping(target = "updatedAt", source = "lesson.updatedAt")
    @Mapping(target = "scopes", source = "scopes")
    @Mapping(target = "assignments", source = "assignments")
    LessonResponse toResponse(
            Lesson lesson, java.util.List<LessonScope> scopes, java.util.List<AssignmentResponse> assignments);

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
    @BeanMapping(nullValuePropertyMappingStrategy = IGNORE)
    void updateEntity(UpdateLessonHeaderRequest request, @MappingTarget Lesson lesson);
}
