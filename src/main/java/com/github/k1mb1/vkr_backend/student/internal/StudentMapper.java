package com.github.k1mb1.vkr_backend.student.internal;

import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

import com.github.k1mb1.vkr_backend.group.GroupReferenceService;
import com.github.k1mb1.vkr_backend.group.domain.Subgroup;
import com.github.k1mb1.vkr_backend.student.domain.Student;
import com.github.k1mb1.vkr_backend.student.internal.web.requests.UpdateStudentRequest;
import com.github.k1mb1.vkr_backend.student.internal.web.response.StudentResponse;
import java.util.UUID;
import org.mapstruct.BeanMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface StudentMapper {
    @Mapping(target = "groupId", source = "group.id")
    @Mapping(target = "subgroupId", source = "subgroup.id")
    StudentResponse toResponse(Student student);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "group", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "archivedAt", ignore = true)
    @Mapping(
        target = "subgroup",
        source = "subgroupId",
        qualifiedByName = "subgroupRef"
    )
    @BeanMapping(nullValuePropertyMappingStrategy = IGNORE)
    void updateEntity(
        UpdateStudentRequest request,
        @MappingTarget Student student,
        @Context GroupReferenceService groupReferenceService
    );

    @Named("subgroupRef")
    static Subgroup toSubgroupRef(
        UUID subgroupId,
        @Context GroupReferenceService groupReferenceService
    ) {
        return subgroupId != null
            ? groupReferenceService.getSubgroupReferenceById(subgroupId)
            : null;
    }

    @org.mapstruct.AfterMapping
    default void afterUpdate(
        UpdateStudentRequest request,
        @MappingTarget Student student
    ) {
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
