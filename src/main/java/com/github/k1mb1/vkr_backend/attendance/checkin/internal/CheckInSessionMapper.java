package com.github.k1mb1.vkr_backend.attendance.checkin.internal;

import com.github.k1mb1.vkr_backend.attendance.checkin.domain.CheckInRecord;
import com.github.k1mb1.vkr_backend.attendance.checkin.domain.CheckInSession;
import com.github.k1mb1.vkr_backend.attendance.checkin.web.responses.CheckInRecordResponse;
import com.github.k1mb1.vkr_backend.attendance.checkin.web.responses.CheckInSessionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
interface CheckInSessionMapper {

    @Mapping(target = "lessonId", source = "session.lesson.id")
    @Mapping(target = "permissionId", source = "session.permission.id")
    @Mapping(target = "startedAt", source = "session.startedAt")
    @Mapping(target = "onTimeSeconds", source = "session.onTimeSeconds")
    @Mapping(target = "lateSeconds", source = "session.lateSeconds")
    @Mapping(target = "onTimeEndsAt", expression = "java(session.onTimeEndsAt())")
    @Mapping(target = "lateEndsAt", expression = "java(session.lateEndsAt())")
    @Mapping(target = "confirmedAt", source = "session.confirmedAt")
    @Mapping(target = "cancelledAt", source = "session.cancelledAt")
    @Mapping(target = "state", expression = "java(session.stateAt(now))")
    @Mapping(target = "id", source = "session.id")
    CheckInSessionResponse toResponse(CheckInSession session, Instant now);

    @Mapping(target = "sessionId", source = "session.id")
    @Mapping(target = "studentId", source = "student.id")
    CheckInRecordResponse toRecordResponse(CheckInRecord record);
}
