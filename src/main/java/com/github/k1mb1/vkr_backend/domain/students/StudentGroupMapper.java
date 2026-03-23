package com.github.k1mb1.vkr_backend.domain.students;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import com.github.k1mb1.vkr_backend.domain.student_groups.StudentGroupEntity;
import com.github.k1mb1.vkr_backend.domain.student_groups.requests.CreateStudentGroupRequest;
import com.github.k1mb1.vkr_backend.domain.student_groups.requests.UpdateStudentGroupRequest;
import com.github.k1mb1.vkr_backend.domain.student_groups.responses.StudentGroupDetailResponse;
import com.github.k1mb1.vkr_backend.domain.student_groups.responses.StudentGroupResponse;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = SPRING)
public interface StudentGroupMapper {
    StudentGroupResponse toResponse(StudentGroupEntity entity);

    @Mapping(
        target = "studentIds",
        expression = "java(entity.getStudents().stream().map(s -> s.getId()).toList())"
    )
    StudentGroupDetailResponse toDetailResponse(StudentGroupEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "students", ignore = true)
    StudentGroupEntity toEntity(CreateStudentGroupRequest request);

    @BeanMapping(
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
    )
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "students", ignore = true)
    void update(
        @MappingTarget StudentGroupEntity entity,
        UpdateStudentGroupRequest request
    );
}
