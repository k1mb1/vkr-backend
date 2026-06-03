package com.github.k1mb1.vkr_backend.attendance.checkin.internal;

import com.github.k1mb1.vkr_backend.common.GeneratedMapper;
import org.mapstruct.AnnotateWith;

import com.github.k1mb1.vkr_backend.attendance.checkin.domain.CheckInRecord;
import com.github.k1mb1.vkr_backend.attendance.checkin.web.responses.CheckInRecordResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@AnnotateWith(GeneratedMapper.class)
@Mapper(componentModel = SPRING)
interface CheckInRecordMapper {

    @Mapping(target = "sessionId", source = "session.id")
    @Mapping(target = "studentId", source = "student.id")
    CheckInRecordResponse toResponse(CheckInRecord record);
}
