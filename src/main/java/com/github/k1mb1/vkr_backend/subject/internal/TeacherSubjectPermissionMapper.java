package com.github.k1mb1.vkr_backend.subject.internal;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

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

@Mapper(componentModel = SPRING)
interface TeacherSubjectPermissionMapper {
    @Mapping(target = "teacherId", source = "teacher.id")
    @Mapping(target = "teacherName", source = "teacher.username")
    @Mapping(target = "subjectId", source = "subject.id")
    @Mapping(target = "scopes", ignore = true)
    TeacherSubjectPermissionResponse toResponse(
        TeacherSubjectPermission permission
    );

    default TeacherSubjectPermissionResponse toFullResponse(
        TeacherSubjectPermission permission
    ) {
        var base = toResponse(permission);
        return TeacherSubjectPermissionResponse.builder()
            .id(base.id())
            .teacherId(base.teacherId())
            .teacherName(base.teacherName())
            .subjectId(base.subjectId())
            .allPermissions(base.allPermissions())
            .scopes(scopesForPermission(permission))
            .createdAt(base.createdAt())
            .updatedAt(base.updatedAt())
            .build();
    }

    default PermissionScopeSubgroupResponse toSubgroup(Subgroup subgroup) {
        if (subgroup == null) {
            return null;
        }
        return PermissionScopeSubgroupResponse.builder()
            .id(subgroup.getId())
            .index(subgroup.getIndex())
            .build();
    }

    default PermissionScopeGroupResponse toGroup(Group group) {
        if (group == null) {
            return null;
        }
        var subgroups = group
            .getSubgroups()
            .stream()
            .sorted(Comparator.comparing(Subgroup::getIndex))
            .map(this::toSubgroup)
            .toList();
        return PermissionScopeGroupResponse.builder()
            .id(group.getId())
            .name(group.getName())
            .subgroups(subgroups)
            .build();
    }

    default PermissionScopeResponse toScopeResponse(PermissionScope scope) {
        return PermissionScopeResponse.builder()
            .id(scope.getId())
            .group(toGroup(scope.getGroup()))
            .allowedSubgroup(toSubgroup(scope.getAllowedSubgroup()))
            .allowedLessonType(scope.getAllowedLessonType())
            .build();
    }

    /**
     * Scopes for the response: empty when allPermissions=true (teacher sees the whole subject;
     * the client should consult subject.groups directly). Otherwise the explicit per-group scopes;
     * all-groups scopes (group=null) sort first.
     */
    default List<PermissionScopeResponse> scopesForPermission(
        TeacherSubjectPermission permission
    ) {
        if (permission.isAllPermissions()) {
            return List.of();
        }
        return permission
            .getScopes()
            .stream()
            .sorted(
                Comparator.comparing(
                    (PermissionScope s) ->
                        s.getGroup() == null ? null : s.getGroup().getName(),
                    Comparator.nullsFirst(Comparator.naturalOrder())
                )
            )
            .map(this::toScopeResponse)
            .toList();
    }
}
