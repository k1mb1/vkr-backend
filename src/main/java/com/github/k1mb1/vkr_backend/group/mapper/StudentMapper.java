package com.github.k1mb1.vkr_backend.group.mapper;

import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

import com.github.k1mb1.vkr_backend.group.GroupReferenceService;
import com.github.k1mb1.vkr_backend.group.domain.StudentEntity;
import com.github.k1mb1.vkr_backend.group.domain.SubgroupEntity;
import com.github.k1mb1.vkr_backend.group.service.dto.request.UpdateStudentRequest;
import com.github.k1mb1.vkr_backend.group.service.dto.response.StudentResponse;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.mapstruct.BeanMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface StudentMapper {
    @Named("subgroupRef")
    static @Nullable SubgroupEntity toSubgroupRef(
            @Nullable UUID subgroupId, @Context GroupReferenceService groupReferenceService) {
        return subgroupId != null ? groupReferenceService.getSubgroupReferenceById(subgroupId) : null;
    }

    @Mapping(target = "groupId", source = "group.id")
    @Mapping(target = "subgroupId", source = "subgroup.id")
    StudentResponse toResponse(StudentEntity student);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "group", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "archivedAt", ignore = true)
    @Mapping(target = "subgroup", source = "subgroupId", qualifiedByName = "subgroupRef")
    @BeanMapping(nullValuePropertyMappingStrategy = IGNORE)
    void updateEntity(
            UpdateStudentRequest request,
            @MappingTarget StudentEntity student,
            @Context GroupReferenceService groupReferenceService);

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
