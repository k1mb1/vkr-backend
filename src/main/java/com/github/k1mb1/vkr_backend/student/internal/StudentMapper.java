package com.github.k1mb1.vkr_backend.student.internal;

import com.github.k1mb1.vkr_backend.common.GeneratedMapper;
import org.mapstruct.AnnotateWith;

import com.github.k1mb1.vkr_backend.group.GroupReferenceService;
import com.github.k1mb1.vkr_backend.group.domain.Subgroup;
import com.github.k1mb1.vkr_backend.student.domain.Student;
import com.github.k1mb1.vkr_backend.student.internal.web.requests.UpdateStudentRequest;
import com.github.k1mb1.vkr_backend.student.internal.web.response.StudentResponse;
import org.mapstruct.*;

import java.util.UUID;

import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@AnnotateWith(GeneratedMapper.class)
@Mapper(componentModel = "spring")
public interface StudentMapper {
    @Named("subgroupRef")
    static Subgroup toSubgroupRef(
        UUID subgroupId,
        @Context GroupReferenceService groupReferenceService
    ) {
        return subgroupId != null
               ? groupReferenceService.getSubgroupReferenceById(subgroupId)
               : null;
    }

    @Mapping(target = "groupId", source = "group.id")
    @Mapping(target = "subgroupId", source = "subgroup.id")
    StudentResponse toResponse(Student student);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "group", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "archivedAt", ignore = true)
    @Mapping(
        target = "subgroup", source = "subgroupId", qualifiedByName = "subgroupRef"
    )
    @BeanMapping(nullValuePropertyMappingStrategy = IGNORE)
    void updateEntity(
        UpdateStudentRequest request,
        @MappingTarget Student student,
        @Context GroupReferenceService groupReferenceService
    );

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
