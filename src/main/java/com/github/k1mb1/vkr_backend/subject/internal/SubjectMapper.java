package com.github.k1mb1.vkr_backend.subject.internal;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;
import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

import com.github.k1mb1.vkr_backend.subject.domain.AssessmentBand;
import com.github.k1mb1.vkr_backend.subject.domain.AttendanceHighlightPolicy;
import com.github.k1mb1.vkr_backend.subject.domain.AttendancePolicy;
import com.github.k1mb1.vkr_backend.subject.domain.CheckInPolicy;
import com.github.k1mb1.vkr_backend.subject.domain.FinalAssessmentPolicy;
import com.github.k1mb1.vkr_backend.subject.domain.GradingHighlightPolicy;
import com.github.k1mb1.vkr_backend.subject.domain.PenaltyPolicy;
import com.github.k1mb1.vkr_backend.subject.domain.Subject;
import com.github.k1mb1.vkr_backend.subject.web.requests.UpdateSubjectRequest;
import com.github.k1mb1.vkr_backend.subject.web.responses.AttendanceHighlightPolicyResponse;
import com.github.k1mb1.vkr_backend.subject.web.responses.AttendancePolicyResponse;
import com.github.k1mb1.vkr_backend.subject.web.responses.CheckInPolicyResponse;
import com.github.k1mb1.vkr_backend.subject.web.responses.FinalAssessmentPolicyResponse;
import com.github.k1mb1.vkr_backend.subject.web.responses.GradingHighlightPolicyResponse;
import com.github.k1mb1.vkr_backend.subject.web.responses.PenaltyPolicyResponse;
import com.github.k1mb1.vkr_backend.subject.web.responses.SubjectPageResponse;
import com.github.k1mb1.vkr_backend.subject.web.responses.SubjectResponse;
import org.mapstruct.*;

@Mapper(componentModel = SPRING)
public interface SubjectMapper {
    SubjectPageResponse toResponse(Subject subject);

    SubjectResponse toFullResponse(Subject subject);

    PenaltyPolicyResponse toPenaltyPolicyResponse(PenaltyPolicy policy);

    AttendancePolicyResponse toAttendancePolicyResponse(
        AttendancePolicy policy
    );

    CheckInPolicyResponse toCheckInPolicyResponse(CheckInPolicy policy);

    GradingHighlightPolicyResponse toGradingHighlightPolicyResponse(
        GradingHighlightPolicy policy
    );

    AttendanceHighlightPolicyResponse toAttendanceHighlightPolicyResponse(
        AttendanceHighlightPolicy policy
    );

    FinalAssessmentPolicyResponse toFinalAssessmentPolicyResponse(
        FinalAssessmentPolicy policy
    );

    @Mapping(target = "id", source = "id")
    FinalAssessmentPolicyResponse.Band toBand(AssessmentBand band);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "archivedAt", ignore = true)
    @Mapping(target = "groups", ignore = true)
    @Mapping(target = "penaltyPolicy", ignore = true)
    @Mapping(target = "attendancePolicy", ignore = true)
    @Mapping(target = "checkInPolicy", ignore = true)
    @Mapping(target = "gradingHighlightPolicy", ignore = true)
    @Mapping(target = "attendanceHighlightPolicy", ignore = true)
    @Mapping(target = "finalAssessmentPolicy", ignore = true)
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
