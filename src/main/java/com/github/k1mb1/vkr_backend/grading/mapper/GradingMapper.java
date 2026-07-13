package com.github.k1mb1.vkr_backend.grading.mapper;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import com.github.k1mb1.vkr_backend.grading.api.AssignmentResponse;
import com.github.k1mb1.vkr_backend.grading.api.GradeCellResponse;
import com.github.k1mb1.vkr_backend.grading.api.GradingTableStudent;
import com.github.k1mb1.vkr_backend.grading.domain.AssignmentAdmissionTier;
import com.github.k1mb1.vkr_backend.grading.domain.AssignmentEntity;
import com.github.k1mb1.vkr_backend.grading.domain.GradeEntity;
import com.github.k1mb1.vkr_backend.lesson.api.LessonStudentResponse;
import com.github.k1mb1.vkr_backend.subject.api.AttendancePolicyResponse;
import com.github.k1mb1.vkr_backend.subject.api.FinalAssessmentPolicyResponse;
import com.github.k1mb1.vkr_backend.subject.api.GradingHighlightPolicyResponse;
import com.github.k1mb1.vkr_backend.subject.api.PenaltyPolicyResponse;
import com.github.k1mb1.vkr_backend.subject.domain.AssessmentBandEntity;
import com.github.k1mb1.vkr_backend.subject.domain.AttendancePolicyEntity;
import com.github.k1mb1.vkr_backend.subject.domain.FinalAssessmentPolicyEntity;
import com.github.k1mb1.vkr_backend.subject.domain.GradingHighlightPolicyEntity;
import com.github.k1mb1.vkr_backend.subject.domain.PenaltyPolicyEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = SPRING)
public interface GradingMapper {
    @Mapping(target = "studentId", source = "student.id")
    @Mapping(target = "lessonId", source = "lesson.id")
    @Mapping(target = "assignmentId", source = "assignment.id")
    @Mapping(target = "awardedLessonId", source = "awardedLesson.id")
    GradeCellResponse toCell(GradeEntity grade);

    @Mapping(target = "lessonId", source = "lesson.id")
    AssignmentResponse toAssignmentResponse(AssignmentEntity assignment);

    @Mapping(target = "bandId", source = "bandId")
    @Mapping(target = "minScore", source = "minScore")
    AssignmentResponse.AdmissionTier toAdmissionTier(AssignmentAdmissionTier tier);

    GradingTableStudent toTableStudent(LessonStudentResponse student);

    // Политики предмета (subject::domain -> subject::api) для шапки таблицы оценок.

    PenaltyPolicyResponse toPenaltyPolicyResponse(PenaltyPolicyEntity policy);

    AttendancePolicyResponse toAttendancePolicyResponse(AttendancePolicyEntity policy);

    GradingHighlightPolicyResponse toGradingHighlightPolicyResponse(GradingHighlightPolicyEntity policy);

    FinalAssessmentPolicyResponse toFinalAssessmentPolicyResponse(FinalAssessmentPolicyEntity policy);

    @Mapping(target = "id", source = "id")
    FinalAssessmentPolicyResponse.Band toBand(AssessmentBandEntity band);
}
