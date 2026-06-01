package com.github.k1mb1.vkr_backend.attendance.internal;

import com.github.k1mb1.vkr_backend.attendance.domain.Attendance;
import com.github.k1mb1.vkr_backend.attendance.web.responses.AttendanceCellResponse;
import com.github.k1mb1.vkr_backend.attendance.web.responses.AttendanceTableLesson;
import com.github.k1mb1.vkr_backend.attendance.web.responses.AttendanceTableStudent;
import com.github.k1mb1.vkr_backend.lesson.domain.LessonScope;
import com.github.k1mb1.vkr_backend.student.domain.Student;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
interface AttendanceMapper {
    @Mapping(target = "studentId", source = "student.id")
    @Mapping(target = "lessonScopeId", source = "lessonScope.id")
    AttendanceCellResponse toCell(Attendance attendance);

    @Mapping(target = "groupId", source = "group.id")
    @Mapping(target = "groupName", source = "group.name")
    @Mapping(target = "subgroupId", source = "subgroup.id")
    @Mapping(target = "subgroupIndex", source = "subgroup.index")
    AttendanceTableStudent toTableStudent(Student student);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "lessonId", source = "lesson.id")
    @Mapping(target = "startedAt", source = "startedAt")
    @Mapping(target = "type", source = "lesson.type")
    @Mapping(target = "orderIndex", source = "lesson.orderIndex")
    @Mapping(target = "topic", source = "lesson.topic")
    @Mapping(target = "active", source = "lesson.active")
    @Mapping(target = "groupId", source = "group.id")
    @Mapping(target = "groupName", source = "group.name")
    @Mapping(target = "allowedSubgroupId", source = "allowedSubgroup.id")
    @Mapping(target = "allowedSubgroupIndex", source = "allowedSubgroup.index")
    @Mapping(target = "allGroups", source = "allGroups")
    AttendanceTableLesson toTableLesson(LessonScope scope);
}
