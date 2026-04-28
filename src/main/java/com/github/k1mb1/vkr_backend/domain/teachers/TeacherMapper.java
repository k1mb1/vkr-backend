package com.github.k1mb1.vkr_backend.domain.teachers;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import com.github.k1mb1.vkr_backend.domain.subjects.SubjectMapper;
import com.github.k1mb1.vkr_backend.domain.teachers.requests.CreateOrUpdateTeacherRequest;
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

    @Mapping(target = "id", source = "id")
    @Mapping(target = "subjects", ignore = true)
    TeacherEntity toEntity(UUID id, CreateOrUpdateTeacherRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "subjects", ignore = true)
    void update(
        @MappingTarget TeacherEntity entity,
        CreateOrUpdateTeacherRequest request
    );
}
