package com.github.k1mb1.vkr_backend.attendance.internal;

import com.github.k1mb1.vkr_backend.attendance.domain.Attendance;
import com.github.k1mb1.vkr_backend.attendance.web.responses.AttendanceCellResponse;
import com.github.k1mb1.vkr_backend.attendance.web.responses.AttendanceTableLesson;
import com.github.k1mb1.vkr_backend.attendance.web.responses.AttendanceTableStudent;
import com.github.k1mb1.vkr_backend.lesson.domain.Lesson;
import com.github.k1mb1.vkr_backend.student.domain.Student;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
interface AttendanceMapper {
    @Mapping(target = "studentId", source = "student.id")
    @Mapping(target = "lessonId", source = "lesson.id")
    AttendanceCellResponse toCell(Attendance attendance);

    @Mapping(target = "subgroupId", source = "subgroup.id")
    AttendanceTableStudent toTableStudent(Student student);

    AttendanceTableLesson toTableLesson(Lesson lesson);
}
