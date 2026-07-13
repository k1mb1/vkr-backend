package com.github.k1mb1.vkr_backend.grading.mapper;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import com.github.k1mb1.vkr_backend.grading.domain.AssignmentAdmissionTier;
import com.github.k1mb1.vkr_backend.grading.domain.AssignmentEntity;
import com.github.k1mb1.vkr_backend.grading.domain.GradeEntity;
import com.github.k1mb1.vkr_backend.grading.service.dto.response.AssignmentResponse;
import com.github.k1mb1.vkr_backend.grading.service.dto.response.GradeCellResponse;
import com.github.k1mb1.vkr_backend.grading.service.dto.response.GradingTableStudent;
import com.github.k1mb1.vkr_backend.group.domain.StudentEntity;
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

    @Mapping(target = "groupId", source = "group.id")
    @Mapping(target = "groupName", source = "group.name")
    @Mapping(target = "subgroupId", source = "subgroup.id")
    @Mapping(target = "subgroupIndex", source = "subgroup.index")
    GradingTableStudent toTableStudent(StudentEntity student);
}
