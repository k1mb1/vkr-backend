package com.github.k1mb1.vkr_backend.attendance.mapper;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import com.github.k1mb1.vkr_backend.attendance.domain.AttendanceEntity;
import com.github.k1mb1.vkr_backend.attendance.service.dto.response.AttendanceCellResponse;
import com.github.k1mb1.vkr_backend.attendance.service.dto.response.AttendanceTableLesson;
import com.github.k1mb1.vkr_backend.attendance.service.dto.response.AttendanceTableStudent;
import com.github.k1mb1.vkr_backend.group.domain.StudentEntity;
import com.github.k1mb1.vkr_backend.lesson.domain.LessonScopeEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = SPRING)
public interface AttendanceMapper {
    @Mapping(target = "studentId", source = "student.id")
    @Mapping(target = "lessonScopeId", source = "lessonScope.id")
    AttendanceCellResponse toCell(AttendanceEntity attendance);

    @Mapping(target = "groupId", source = "group.id")
    @Mapping(target = "groupName", source = "group.name")
    @Mapping(target = "subgroupId", source = "subgroup.id")
    @Mapping(target = "subgroupIndex", source = "subgroup.index")
    AttendanceTableStudent toTableStudent(StudentEntity student);

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
    AttendanceTableLesson toTableLesson(LessonScopeEntity scope);
}
