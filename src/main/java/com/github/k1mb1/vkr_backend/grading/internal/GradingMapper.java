package com.github.k1mb1.vkr_backend.grading.internal;

import com.github.k1mb1.vkr_backend.common.GeneratedMapper;
import org.mapstruct.AnnotateWith;

import com.github.k1mb1.vkr_backend.grading.domain.Assignment;
import com.github.k1mb1.vkr_backend.grading.domain.Grade;
import com.github.k1mb1.vkr_backend.grading.web.responses.AssignmentResponse;
import com.github.k1mb1.vkr_backend.grading.web.responses.GradeCellResponse;
import com.github.k1mb1.vkr_backend.grading.web.responses.GradingTableStudent;
import com.github.k1mb1.vkr_backend.student.domain.Student;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@AnnotateWith(GeneratedMapper.class)
@Mapper(componentModel = SPRING)
interface GradingMapper {

    @Mapping(target = "studentId", source = "student.id")
    @Mapping(target = "lessonId", source = "lesson.id")
    @Mapping(target = "assignmentId", source = "assignment.id")
    @Mapping(target = "awardedLessonId", source = "awardedLesson.id")
    GradeCellResponse toCell(Grade grade);

    @Mapping(target = "lessonId", source = "lesson.id")
    AssignmentResponse toAssignmentResponse(Assignment assignment);

    @Mapping(target = "groupId", source = "group.id")
    @Mapping(target = "groupName", source = "group.name")
    @Mapping(target = "subgroupId", source = "subgroup.id")
    @Mapping(target = "subgroupIndex", source = "subgroup.index")
    GradingTableStudent toTableStudent(Student student);
}
