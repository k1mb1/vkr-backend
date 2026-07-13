package com.github.k1mb1.vkr_backend.group.mapper;

import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

import com.github.k1mb1.vkr_backend.group.domain.StudentEntity;
import com.github.k1mb1.vkr_backend.group.service.dto.request.UpdateStudentRequest;
import com.github.k1mb1.vkr_backend.group.service.dto.response.StudentResponse;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface StudentMapper {
    @Mapping(target = "groupId", source = "group.id")
    @Mapping(target = "subgroupId", source = "subgroup.id")
    StudentResponse toResponse(StudentEntity student);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "group", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "archivedAt", ignore = true)
    // Подгруппа — ссылка на сущность; её разрешает сервис (мапперу не место у репозитория).
    @Mapping(target = "subgroup", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = IGNORE)
    void updateEntity(UpdateStudentRequest request, @MappingTarget StudentEntity student);

    @org.mapstruct.AfterMapping
    default void afterUpdate(UpdateStudentRequest request, @MappingTarget StudentEntity student) {
        if (request.archived() == null) {
            return;
        }
        if (request.archived()) {
            student.archive();
        } else {
            student.unarchive();
        }
    }
}
