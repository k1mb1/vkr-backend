package com.github.k1mb1.vkr_backend.attendance.checkin.internal;

import com.github.k1mb1.vkr_backend.attendance.checkin.domain.CheckInRecord;
import com.github.k1mb1.vkr_backend.attendance.checkin.domain.CheckInSession;
import com.github.k1mb1.vkr_backend.attendance.checkin.web.responses.CheckInAudienceScope;
import com.github.k1mb1.vkr_backend.attendance.checkin.web.responses.CheckInRecordResponse;
import com.github.k1mb1.vkr_backend.attendance.checkin.web.responses.CheckInSessionResponse;
import com.github.k1mb1.vkr_backend.lesson.domain.Lesson;
import java.util.Comparator;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
interface CheckInSessionMapper {

    @Mapping(target = "id", source = "session.id")
    @Mapping(target = "lessonId", source = "session.lesson.id")
    @Mapping(target = "allGroups", expression = "java(session.getLesson().isAllGroups())")
    @Mapping(target = "audience", expression = "java(audienceOf(session.getLesson()))")
    @Mapping(target = "startedAt", source = "session.startedAt")
    @Mapping(target = "onTimeSeconds", source = "session.onTimeSeconds")
    @Mapping(target = "lateSeconds", source = "session.lateSeconds")
    @Mapping(target = "onTimeEndsAt", expression = "java(session.onTimeEndsAt())")
    @Mapping(target = "lateEndsAt", expression = "java(session.lateEndsAt())")
    @Mapping(target = "confirmedAt", source = "session.confirmedAt")
    @Mapping(target = "cancelledAt", source = "session.cancelledAt")
    @Mapping(target = "state", expression = "java(session.stateAt(now))")
    CheckInSessionResponse toResponse(CheckInSession session, Instant now);

    @Mapping(target = "sessionId", source = "session.id")
    @Mapping(target = "studentId", source = "student.id")
    CheckInRecordResponse toRecordResponse(CheckInRecord record);

    default List<CheckInAudienceScope> audienceOf(Lesson lesson) {
        if (lesson.isAllGroups()) {
            return lesson.getSubject().getGroups().stream()
                .sorted(Comparator.comparing(g -> g.getName()))
                .map(g -> new CheckInAudienceScope(g.getId(), g.getName(), null, null))
                .toList();
        }
        return lesson.getScopes().stream()
            .sorted(Comparator
                .comparing((com.github.k1mb1.vkr_backend.lesson.domain.LessonScope s) -> s.getGroup().getName())
                .thenComparing(s -> s.getAllowedSubgroup() == null ? -1 : s.getAllowedSubgroup().getIndex()))
            .map(s -> new CheckInAudienceScope(
                s.getGroup().getId(),
                s.getGroup().getName(),
                s.getAllowedSubgroup() != null ? s.getAllowedSubgroup().getId() : null,
                s.getAllowedSubgroup() != null ? s.getAllowedSubgroup().getIndex() : null
            ))
            .toList();
    }
}
