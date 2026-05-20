package com.github.k1mb1.vkr_backend.grading.internal;

import com.github.k1mb1.vkr_backend.grading.domain.Assignment;
import com.github.k1mb1.vkr_backend.grading.domain.Grade;
import com.github.k1mb1.vkr_backend.grading.web.responses.AssignmentResponse;
import com.github.k1mb1.vkr_backend.grading.web.responses.GradeCellResponse;
import com.github.k1mb1.vkr_backend.grading.web.responses.GradingTableLesson;
import com.github.k1mb1.vkr_backend.grading.web.responses.GradingTableStudent;
import com.github.k1mb1.vkr_backend.lesson.domain.Lesson;
import com.github.k1mb1.vkr_backend.student.domain.Student;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
interface GradingMapper {

    @Mapping(target = "studentId", source = "student.id")
    @Mapping(target = "lessonId", source = "lesson.id")
    @Mapping(target = "assignmentId", source = "assignment.id")
    GradeCellResponse toCell(Grade grade);

    @Mapping(target = "lessonId", source = "lesson.id")
    AssignmentResponse toAssignmentResponse(Assignment assignment);

    @Mapping(target = "groupId", source = "group.id")
    @Mapping(target = "groupName", source = "group.name")
    @Mapping(target = "subgroupId", source = "subgroup.id")
    @Mapping(target = "subgroupIndex", source = "subgroup.index")
    GradingTableStudent toTableStudent(Student student);

    GradingTableLesson toTableLesson(Lesson lesson);
}
