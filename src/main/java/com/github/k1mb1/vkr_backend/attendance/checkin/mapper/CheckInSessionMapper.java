package com.github.k1mb1.vkr_backend.attendance.checkin.mapper;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import com.github.k1mb1.vkr_backend.attendance.checkin.domain.CheckInSessionEntity;
import com.github.k1mb1.vkr_backend.attendance.checkin.service.dto.response.CheckInAudienceScopeResponse;
import com.github.k1mb1.vkr_backend.attendance.checkin.service.dto.response.CheckInSessionResponse;
import com.github.k1mb1.vkr_backend.lesson.domain.LessonScopeEntity;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = SPRING)
public interface CheckInSessionMapper {
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
    CheckInSessionResponse toResponse(CheckInSessionEntity session, Instant now);

    default List<CheckInAudienceScopeResponse> audienceOf(LessonScopeEntity scope) {
        if (scope.isAllGroups()) {
            return scope.getLesson().getSubject().getGroups().stream()
                    .sorted(Comparator.comparing(g -> g.getName()))
                    .map(g -> CheckInAudienceScopeResponse.builder()
                            .groupId(g.getId())
                            .groupName(g.getName())
                            .allowedSubgroupId(null)
                            .allowedSubgroupIndex(null)
                            .build())
                    .toList();
        }
        if (scope.getGroup() == null) {
            return List.of();
        }
        return List.of(CheckInAudienceScopeResponse.builder()
                .groupId(scope.getGroup().getId())
                .groupName(scope.getGroup().getName())
                .allowedSubgroupId(
                        scope.getAllowedSubgroup() != null
                                ? scope.getAllowedSubgroup().getId()
                                : null)
                .allowedSubgroupIndex(
                        scope.getAllowedSubgroup() != null
                                ? scope.getAllowedSubgroup().getIndex()
                                : null)
                .build());
    }
}
