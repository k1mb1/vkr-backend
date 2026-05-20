package com.github.k1mb1.vkr_backend.grade.internal;

import com.github.k1mb1.vkr_backend.grade.domain.Grade;
import com.github.k1mb1.vkr_backend.grade.web.responses.GradeCellResponse;
import com.github.k1mb1.vkr_backend.grade.web.responses.GradeTableLesson;
import com.github.k1mb1.vkr_backend.grade.web.responses.GradeTableStudent;
import com.github.k1mb1.vkr_backend.lesson.domain.Lesson;
import com.github.k1mb1.vkr_backend.student.domain.Student;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
interface GradeMapper {

    @Mapping(target = "studentId", source = "student.id")
    @Mapping(target = "assignmentId", source = "assignment.id")
    GradeCellResponse toCell(Grade grade);

    @Mapping(target = "groupId", source = "group.id")
    @Mapping(target = "groupName", source = "group.name")
    @Mapping(target = "subgroupId", source = "subgroup.id")
    @Mapping(target = "subgroupIndex", source = "subgroup.index")
    GradeTableStudent toTableStudent(Student student);

    GradeTableLesson toTableLesson(Lesson lesson);
}
