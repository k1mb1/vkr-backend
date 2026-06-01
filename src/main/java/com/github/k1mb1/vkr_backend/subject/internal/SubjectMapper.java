package com.github.k1mb1.vkr_backend.subject.internal;

import com.github.k1mb1.vkr_backend.subject.domain.AttendancePolicy;
import com.github.k1mb1.vkr_backend.subject.domain.PenaltyPolicy;
import com.github.k1mb1.vkr_backend.subject.domain.Subject;
import com.github.k1mb1.vkr_backend.subject.web.requests.UpdateSubjectRequest;
import com.github.k1mb1.vkr_backend.subject.web.responses.AttendancePolicyResponse;
import com.github.k1mb1.vkr_backend.subject.web.responses.PenaltyPolicyResponse;
import com.github.k1mb1.vkr_backend.subject.web.responses.SubjectPageResponse;
import com.github.k1mb1.vkr_backend.subject.web.responses.SubjectResponse;
import org.mapstruct.*;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;
import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(componentModel = SPRING)
interface SubjectMapper {
    SubjectPageResponse toResponse(Subject subject);

    SubjectResponse toFullResponse(Subject subject);

    PenaltyPolicyResponse toPenaltyPolicyResponse(PenaltyPolicy policy);

    AttendancePolicyResponse toAttendancePolicyResponse(AttendancePolicy policy);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "archivedAt", ignore = true)
    @Mapping(target = "groups", ignore = true)
    @Mapping(target = "penaltyPolicy", ignore = true)
    @Mapping(target = "attendancePolicy", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = IGNORE)
    void updateEntity(
        UpdateSubjectRequest request,
        @MappingTarget Subject subject
    );

    @AfterMapping
    default void afterUpdate(
        UpdateSubjectRequest request,
        @MappingTarget Subject subject
    ) {
        if (request.archived() == null) {
            return;
        }
        if (request.archived()) {
            subject.archive();
        } else {
            subject.unarchive();
        }
    }
}
