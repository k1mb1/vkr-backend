package com.github.k1mb1.vkr_backend.domain.attendances;

import com.github.k1mb1.vkr_backend.domain.attendances.requests.CreateAttendanceRequest;
import com.github.k1mb1.vkr_backend.domain.attendances.requests.UpdateAttendanceRequest;
import com.github.k1mb1.vkr_backend.domain.attendances.responses.AttendanceResponse;
import org.mapstruct.*;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = SPRING)
public interface AttendanceMapper {

    @Mapping(source = "lesson.id", target = "lessonId")
    @Mapping(source = "student.id", target = "studentId")
    AttendanceResponse toResponse(AttendanceEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "lesson", ignore = true)
    @Mapping(target = "student", ignore = true)
    AttendanceEntity toEntity(CreateAttendanceRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "lesson", ignore = true)
    @Mapping(target = "student", ignore = true)
    void update(@MappingTarget AttendanceEntity entity, UpdateAttendanceRequest request);
}