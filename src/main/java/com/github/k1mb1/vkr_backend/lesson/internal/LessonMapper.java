package com.github.k1mb1.vkr_backend.lesson.internal;

import com.github.k1mb1.vkr_backend.lesson.domain.Lesson;
import com.github.k1mb1.vkr_backend.lesson.web.requests.UpdateLessonRequest;
import com.github.k1mb1.vkr_backend.lesson.web.responses.LessonResponse;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;
import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(componentModel = SPRING)
interface LessonMapper {

    @Mapping(target = "offeringId", source = "offering.id")
    @Mapping(target = "subjectId", source = "offering.subject.id")
    @Mapping(target = "groupId", source = "offering.group.id")
    @Mapping(target = "subgroupId", source = "subgroup.id")
    @Mapping(target = "teacherId", source = "teacher.id")
    LessonResponse toResponse(Lesson lesson);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "offering", ignore = true)
    @Mapping(target = "subgroup", ignore = true)
    @Mapping(target = "teacher", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = IGNORE)
    void updateEntity(
        UpdateLessonRequest request,
        @MappingTarget Lesson lesson
    );
}
