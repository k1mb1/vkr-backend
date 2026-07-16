package com.github.k1mb1.vkr_backend.teacher.mapper;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import com.github.k1mb1.vkr_backend.group.domain.GroupEntity;
import com.github.k1mb1.vkr_backend.group.domain.SubgroupEntity;
import com.github.k1mb1.vkr_backend.teacher.domain.PermissionScopeEntity;
import com.github.k1mb1.vkr_backend.teacher.domain.TeacherSubjectPermissionEntity;
import com.github.k1mb1.vkr_backend.teacher.service.dto.response.PermissionScopeGroupResponse;
import com.github.k1mb1.vkr_backend.teacher.service.dto.response.PermissionScopeResponse;
import com.github.k1mb1.vkr_backend.teacher.service.dto.response.PermissionScopeSubgroupResponse;
import com.github.k1mb1.vkr_backend.teacher.service.dto.response.TeacherSubjectPermissionResponse;
import java.util.Comparator;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = SPRING)
public interface TeacherSubjectPermissionMapper {
    @Mapping(target = "teacherId", source = "teacher.id")
    @Mapping(target = "teacherName", source = "teacher.username")
    @Mapping(target = "subjectId", source = "subject.id")
    @Mapping(target = "scopes", ignore = true)
    TeacherSubjectPermissionResponse toResponse(TeacherSubjectPermissionEntity permission);

    default TeacherSubjectPermissionResponse toFullResponse(TeacherSubjectPermissionEntity permission) {
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

    default @Nullable PermissionScopeSubgroupResponse toSubgroup(@Nullable SubgroupEntity subgroup) {
        if (subgroup == null) {
            return null;
        }
        return PermissionScopeSubgroupResponse.builder()
                .id(subgroup.getId())
                .index(subgroup.getIndex())
                .build();
    }

    default @Nullable PermissionScopeGroupResponse toGroup(@Nullable GroupEntity group) {
        if (group == null) {
            return null;
        }
        var subgroups = group.getSubgroups().stream()
                .sorted(Comparator.comparing(SubgroupEntity::getIndex))
                .map(this::toSubgroup)
                .toList();
        return PermissionScopeGroupResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .subgroups(subgroups)
                .build();
    }

    default PermissionScopeResponse toScopeResponse(PermissionScopeEntity scope) {
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
    default List<PermissionScopeResponse> scopesForPermission(TeacherSubjectPermissionEntity permission) {
        if (permission.isAllPermissions()) {
            return List.of();
        }
        return permission.getScopes().stream()
                .sorted(Comparator.comparing(
                        (PermissionScopeEntity s) ->
                                s.getGroup() == null ? null : s.getGroup().getName(),
                        Comparator.nullsFirst(Comparator.naturalOrder())))
                .map(this::toScopeResponse)
                .toList();
    }
}
