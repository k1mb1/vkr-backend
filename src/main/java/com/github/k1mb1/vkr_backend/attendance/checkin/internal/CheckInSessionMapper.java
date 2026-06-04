package com.github.k1mb1.vkr_backend.attendance.checkin.internal;

import com.github.k1mb1.vkr_backend.attendance.checkin.domain.CheckInSession;
import com.github.k1mb1.vkr_backend.attendance.checkin.web.responses.CheckInAudienceScope;
import com.github.k1mb1.vkr_backend.attendance.checkin.web.responses.CheckInSessionResponse;
import com.github.k1mb1.vkr_backend.lesson.domain.LessonScope;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
interface CheckInSessionMapper {

    @Mapping(target = "id", source = "session.id")
    @Mapping(target = "lessonId", source = "session.lessonScope.lesson.id")
    @Mapping(target = "lessonScopeId", source = "session.lessonScope.id")
    @Mapping(target = "allGroups", expression = "java(session.getLessonScope().isAllGroups())")
    @Mapping(target = "audience", expression = "java(audienceOf(session.getLessonScope()))")
    @Mapping(target = "code", source = "session.code")
    @Mapping(target = "startedAt", source = "session.startedAt")
    @Mapping(target = "onTimeSeconds", source = "session.onTimeSeconds")
    @Mapping(target = "lateSeconds", source = "session.lateSeconds")
    @Mapping(target = "onTimeEndsAt", expression = "java(session.onTimeEndsAt())")
    @Mapping(target = "lateEndsAt", expression = "java(session.lateEndsAt())")
    @Mapping(target = "confirmedAt", source = "session.confirmedAt")
    @Mapping(target = "cancelledAt", source = "session.cancelledAt")
    @Mapping(target = "state", expression = "java(session.stateAt(now))")
    CheckInSessionResponse toResponse(CheckInSession session, Instant now);

    default List<CheckInAudienceScope> audienceOf(LessonScope scope) {
        if (scope.isAllGroups()) {
            return scope.getLesson()
                .getSubject()
                .getGroups()
                .stream()
                .sorted(Comparator.comparing(g -> g.getName()))
                .map(g -> new CheckInAudienceScope(g.getId(), g.getName(), null, null))
                .toList();
        }
        if (scope.getGroup() == null) {
            return List.of();
        }
        return List.of(new CheckInAudienceScope(
            scope.getGroup().getId(),
            scope.getGroup().getName(),
            scope.getAllowedSubgroup() != null
            ? scope.getAllowedSubgroup().getId()
            : null,
            scope.getAllowedSubgroup() != null
            ? scope.getAllowedSubgroup().getIndex()
            : null
        ));
    }
}
