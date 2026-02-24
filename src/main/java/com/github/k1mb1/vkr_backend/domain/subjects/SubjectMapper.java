package com.github.k1mb1.vkr_backend.domain.subjects;

import com.github.k1mb1.vkr_backend.domain.lessons.LessonMapper;
import com.github.k1mb1.vkr_backend.domain.students.StudentMapper;
import com.github.k1mb1.vkr_backend.domain.subjects.requests.CreateSubjectRequest;
import com.github.k1mb1.vkr_backend.domain.subjects.requests.UpdateSubjectRequest;
import com.github.k1mb1.vkr_backend.domain.subjects.responses.SubjectDetailsResponse;
import com.github.k1mb1.vkr_backend.domain.subjects.responses.SubjectResponse;
import org.mapstruct.*;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = SPRING, uses = {StudentMapper.class, LessonMapper.class})
public interface SubjectMapper {

    SubjectResponse toResponse(SubjectEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "students", ignore = true)
    @Mapping(target = "teachers", ignore = true)
    @Mapping(target = "lessons", ignore = true)
    SubjectEntity toEntity(CreateSubjectRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "students", ignore = true)
    @Mapping(target = "teachers", ignore = true)
    @Mapping(target = "lessons", ignore = true)
    void update(@MappingTarget SubjectEntity entity, UpdateSubjectRequest request);

    SubjectEntity toEntity(SubjectResponse subjectResponse);


    @AfterMapping
    default void linkLessons(@MappingTarget SubjectEntity subjectEntity) {
        subjectEntity.getLessons().forEach(lesson -> lesson.setSubject(subjectEntity));
    }

    @InheritInverseConfiguration(name = "toEntity")
    SubjectDetailsResponse toDetailsResponse(SubjectEntity subjectEntity);

}