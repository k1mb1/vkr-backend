package com.github.k1mb1.vkr_backend.domain.student_grades;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import com.github.k1mb1.vkr_backend.domain.student_grades.requests.CreateStudentGradeRequest;
import com.github.k1mb1.vkr_backend.domain.student_grades.requests.UpdateStudentGradeRequest;
import com.github.k1mb1.vkr_backend.domain.student_grades.responses.StudentGradeResponse;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = SPRING)
public interface StudentGradeMapper {
    @Mapping(source = "lesson.id", target = "lessonId")
    @Mapping(source = "student.id", target = "studentId")
    StudentGradeResponse toResponse(StudentGradeEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "lesson", ignore = true)
    @Mapping(target = "student", ignore = true)
    StudentGradeEntity toEntity(CreateStudentGradeRequest request);

    @BeanMapping(
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
    )
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "lesson", ignore = true)
    @Mapping(target = "student", ignore = true)
    void update(
        @MappingTarget StudentGradeEntity entity,
        UpdateStudentGradeRequest request
    );
}
