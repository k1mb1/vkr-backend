package com.github.k1mb1.vkr_backend.domain.teachers;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import com.github.k1mb1.vkr_backend.domain.subjects.SubjectMapper;
import com.github.k1mb1.vkr_backend.domain.teachers.requests.CreateTeacherRequest;
import com.github.k1mb1.vkr_backend.domain.teachers.requests.UpdateTeacherRequest;
import com.github.k1mb1.vkr_backend.domain.teachers.responses.TeacherDetailsResponse;
import com.github.k1mb1.vkr_backend.domain.teachers.responses.TeacherResponse;
import java.util.UUID;
import org.mapstruct.*;

@Mapper(
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    componentModel = SPRING,
    uses = { SubjectMapper.class }
)
public interface TeacherMapper {
    TeacherResponse toResponse(TeacherEntity entity);

    @Mapping(target = "subjects", ignore = true)
    TeacherEntity toEntity(CreateTeacherRequest request);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "subjects", ignore = true)
    TeacherEntity toEntity(UUID id, UpdateTeacherRequest request);

    @InheritInverseConfiguration(name = "toEntity")
    TeacherDetailsResponse toDetailsResponse(TeacherEntity teacherEntity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "subjects", ignore = true)
    void update(
        @MappingTarget TeacherEntity entity,
        UpdateTeacherRequest request
    );
}
