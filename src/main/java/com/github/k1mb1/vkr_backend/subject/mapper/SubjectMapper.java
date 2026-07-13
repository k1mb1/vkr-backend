package com.github.k1mb1.vkr_backend.subject.mapper;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;
import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

import com.github.k1mb1.vkr_backend.subject.api.AttendanceHighlightPolicyResponse;
import com.github.k1mb1.vkr_backend.subject.api.AttendancePolicyResponse;
import com.github.k1mb1.vkr_backend.subject.api.CheckInPolicyResponse;
import com.github.k1mb1.vkr_backend.subject.api.FinalAssessmentPolicyResponse;
import com.github.k1mb1.vkr_backend.subject.api.GradingHighlightPolicyResponse;
import com.github.k1mb1.vkr_backend.subject.api.PenaltyPolicyResponse;
import com.github.k1mb1.vkr_backend.subject.domain.AssessmentBandEntity;
import com.github.k1mb1.vkr_backend.subject.domain.AttendanceHighlightPolicyEntity;
import com.github.k1mb1.vkr_backend.subject.domain.AttendancePolicyEntity;
import com.github.k1mb1.vkr_backend.subject.domain.CheckInPolicyEntity;
import com.github.k1mb1.vkr_backend.subject.domain.FinalAssessmentPolicyEntity;
import com.github.k1mb1.vkr_backend.subject.domain.GradingHighlightPolicyEntity;
import com.github.k1mb1.vkr_backend.subject.domain.PenaltyPolicyEntity;
import com.github.k1mb1.vkr_backend.subject.domain.SubjectEntity;
import com.github.k1mb1.vkr_backend.subject.service.dto.request.UpdateSubjectRequest;
import com.github.k1mb1.vkr_backend.subject.service.dto.response.SubjectPageResponse;
import com.github.k1mb1.vkr_backend.subject.service.dto.response.SubjectResponse;
import org.mapstruct.AfterMapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = SPRING)
public interface SubjectMapper {
    SubjectPageResponse toResponse(SubjectEntity subject);

    SubjectResponse toFullResponse(SubjectEntity subject);

    PenaltyPolicyResponse toPenaltyPolicyResponse(PenaltyPolicyEntity policy);

    AttendancePolicyResponse toAttendancePolicyResponse(AttendancePolicyEntity policy);

    CheckInPolicyResponse toCheckInPolicyResponse(CheckInPolicyEntity policy);

    GradingHighlightPolicyResponse toGradingHighlightPolicyResponse(GradingHighlightPolicyEntity policy);

    AttendanceHighlightPolicyResponse toAttendanceHighlightPolicyResponse(AttendanceHighlightPolicyEntity policy);

    FinalAssessmentPolicyResponse toFinalAssessmentPolicyResponse(FinalAssessmentPolicyEntity policy);

    @Mapping(target = "id", source = "id")
    FinalAssessmentPolicyResponse.Band toBand(AssessmentBandEntity band);

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
    void updateEntity(UpdateSubjectRequest request, @MappingTarget SubjectEntity subject);

    @AfterMapping
    default void afterUpdate(UpdateSubjectRequest request, @MappingTarget SubjectEntity subject) {
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
