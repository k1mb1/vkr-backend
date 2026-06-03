package com.github.k1mb1.vkr_backend.subject.internal;

import com.github.k1mb1.vkr_backend.common.GeneratedMapper;
import org.mapstruct.AnnotateWith;

import com.github.k1mb1.vkr_backend.group.domain.Group;
import com.github.k1mb1.vkr_backend.group.domain.Subgroup;
import com.github.k1mb1.vkr_backend.subject.domain.PermissionScope;
import com.github.k1mb1.vkr_backend.subject.domain.TeacherSubjectPermission;
import com.github.k1mb1.vkr_backend.subject.web.responses.PermissionScopeGroupResponse;
import com.github.k1mb1.vkr_backend.subject.web.responses.PermissionScopeResponse;
import com.github.k1mb1.vkr_backend.subject.web.responses.PermissionScopeSubgroupResponse;
import com.github.k1mb1.vkr_backend.subject.web.responses.TeacherSubjectPermissionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Comparator;
import java.util.List;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@AnnotateWith(GeneratedMapper.class)
@Mapper(componentModel = SPRING)
interface TeacherSubjectPermissionMapper {
    @Mapping(target = "teacherId", source = "teacher.id")
    @Mapping(target = "teacherName", source = "teacher.username")
    @Mapping(target = "subjectId", source = "subject.id")
    @Mapping(target = "scopes", ignore = true)
    TeacherSubjectPermissionResponse toResponse(TeacherSubjectPermission permission);

    default PermissionScopeSubgroupResponse toSubgroup(Subgroup subgroup) {
        if (subgroup == null) {
            return null;
        }
        return new PermissionScopeSubgroupResponse(subgroup.getId(), subgroup.getIndex());
    }

    default PermissionScopeGroupResponse toGroup(Group group) {
        if (group == null) {
            return null;
        }
        var subgroups = group.getSubgroups()
            .stream()
            .sorted(Comparator.comparing(Subgroup::getIndex))
            .map(this::toSubgroup)
            .toList();
        return new PermissionScopeGroupResponse(group.getId(), group.getName(), subgroups);
    }

    default PermissionScopeResponse toScopeResponse(PermissionScope scope) {
        return new PermissionScopeResponse(
            scope.getId(),
            toGroup(scope.getGroup()),
            toSubgroup(scope.getAllowedSubgroup()),
            scope.getAllowedLessonType()
        );
    }

    /**
     * Scopes for the response: empty when allPermissions=true (teacher sees the whole subject;
     * the client should consult subject.groups directly). Otherwise the explicit per-group scopes;
     * all-groups scopes (group=null) sort first.
     */
    default List<PermissionScopeResponse> scopesForPermission(TeacherSubjectPermission permission) {
        if (permission.isAllPermissions()) {
            return List.of();
        }
        return permission.getScopes()
            .stream()
            .sorted(Comparator.comparing(
                (PermissionScope s) -> s.getGroup() == null
                                       ? null
                                       : s.getGroup().getName(),
                Comparator.nullsFirst(Comparator.naturalOrder())
            ))
            .map(this::toScopeResponse)
            .toList();
    }
}
