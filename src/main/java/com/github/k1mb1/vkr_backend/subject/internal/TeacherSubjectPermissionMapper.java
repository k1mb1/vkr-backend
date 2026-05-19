package com.github.k1mb1.vkr_backend.subject.internal;

import com.github.k1mb1.vkr_backend.group.domain.Group;
import com.github.k1mb1.vkr_backend.group.domain.Subgroup;
import com.github.k1mb1.vkr_backend.subject.domain.PermissionScope;
import com.github.k1mb1.vkr_backend.subject.domain.TeacherSubjectPermission;
import com.github.k1mb1.vkr_backend.subject.web.responses.PermissionScopeGroupResponse;
import com.github.k1mb1.vkr_backend.subject.web.responses.PermissionScopeResponse;
import com.github.k1mb1.vkr_backend.subject.web.responses.PermissionScopeSubgroupResponse;
import com.github.k1mb1.vkr_backend.subject.web.responses.TeacherSubjectPermissionResponse;
import java.util.Comparator;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

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

    default PermissionScopeResponse syntheticScopeForGroup(Group group) {
        return new PermissionScopeResponse(
            null,
            toGroup(group),
            null,
            null
        );
    }

    default List<PermissionScopeResponse> scopesForPermission(TeacherSubjectPermission permission) {
        if (permission.isAllPermissions()) {
            return permission.getSubject().getGroups()
                .stream()
                .sorted(Comparator.comparing(Group::getName))
                .map(this::syntheticScopeForGroup)
                .toList();
        }
        return permission.getScopes()
            .stream()
            .sorted(Comparator.comparing((PermissionScope s) -> s.getGroup().getName()))
            .map(this::toScopeResponse)
            .toList();
    }
}
