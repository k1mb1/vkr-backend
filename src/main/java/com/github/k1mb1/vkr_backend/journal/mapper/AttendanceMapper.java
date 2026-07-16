package com.github.k1mb1.vkr_backend.journal.mapper;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import com.github.k1mb1.vkr_backend.journal.domain.AttendanceEntity;
import com.github.k1mb1.vkr_backend.journal.service.dto.response.AttendanceCellResponse;
import com.github.k1mb1.vkr_backend.journal.service.dto.response.AttendanceTableLessonResponse;
import com.github.k1mb1.vkr_backend.journal.service.dto.response.AttendanceTableStudentResponse;
import com.github.k1mb1.vkr_backend.lesson.api.LessonStudentResponse;
import com.github.k1mb1.vkr_backend.lesson.domain.LessonScopeEntity;
import com.github.k1mb1.vkr_backend.subject.api.AttendanceHighlightPolicyResponse;
import com.github.k1mb1.vkr_backend.subject.domain.AttendanceHighlightPolicyEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = SPRING)
public interface AttendanceMapper {
    @Mapping(target = "studentId", source = "student.id")
    @Mapping(target = "lessonScopeId", source = "lessonScope.id")
    AttendanceCellResponse toCell(AttendanceEntity attendance);

    AttendanceTableStudentResponse toTableStudent(LessonStudentResponse student);

    /** Политика подсветки предмета (subject::domain -> subject::api) для шапки таблицы. */
    AttendanceHighlightPolicyResponse toHighlightPolicyResponse(AttendanceHighlightPolicyEntity policy);

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
    AttendanceTableLessonResponse toTableLesson(LessonScopeEntity scope);
}
