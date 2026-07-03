package com.github.k1mb1.vkr_backend.subject.internal;

import com.github.k1mb1.vkr_backend.common.error.ConflictException;
import com.github.k1mb1.vkr_backend.common.error.ResourceNotFoundException;
import com.github.k1mb1.vkr_backend.group.GroupReferenceService;
import com.github.k1mb1.vkr_backend.group.domain.Group;
import com.github.k1mb1.vkr_backend.group.domain.Subgroup;
import com.github.k1mb1.vkr_backend.subject.TeacherSubjectPermissionsApi;
import com.github.k1mb1.vkr_backend.subject.domain.PermissionScope;
import com.github.k1mb1.vkr_backend.subject.domain.Subject;
import com.github.k1mb1.vkr_backend.subject.domain.TeacherSubjectPermission;
import com.github.k1mb1.vkr_backend.subject.web.requests.CreateTeacherSubjectPermissionRequest;
import com.github.k1mb1.vkr_backend.subject.web.requests.PermissionScopeRequest;
import com.github.k1mb1.vkr_backend.subject.web.requests.UpdateTeacherSubjectPermissionRequest;
import com.github.k1mb1.vkr_backend.subject.web.responses.TeacherSubjectPermissionResponse;
import com.github.k1mb1.vkr_backend.teacher.TeacherReferenceService;
import jakarta.persistence.EntityNotFoundException;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class TeacherSubjectPermissionService implements TeacherSubjectPermissionsApi {

    final TeacherSubjectPermissionRepository permissionRepository;

    final TeacherReferenceService teacherReferenceService;

    final GroupReferenceService groupReferenceService;

    final SubjectRepository subjectRepository;

    final TeacherSubjectPermissionMapper permissionMapper;

    @Override
    @PreAuthorize("@authz.canManageSubject(#subjectId)")
    public List<TeacherSubjectPermissionResponse> getPermissionsBySubject(
        UUID subjectId
    ) {
        return permissionRepository
            .findAllBySubjectId(subjectId)
            .stream()
            .map(permissionMapper::toFullResponse)
            .toList();
    }

    @Override
    @PreAuthorize("@authz.isSelfOrAdmin(#teacherId) or @authz.canManageSubject(#subjectId)")
    public TeacherSubjectPermissionResponse getPermission(
        UUID subjectId,
        UUID teacherId
    ) {
        return permissionRepository
            .findBySubjectIdAndTeacherId(subjectId, teacherId)
            .map(permissionMapper::toFullResponse)
            .orElseThrow(() ->
                new EntityNotFoundException(
                    "TeacherSubjectPermission not found for subjectId=" +
                        subjectId +
                        ", teacherId=" +
                        teacherId
                )
            );
    }

    @Transactional
    @Override
    @PreAuthorize("@authz.canManageSubject(#request.subjectId())")
    @CacheEvict(cacheNames = "userPermissions", allEntries = true)
    public TeacherSubjectPermissionResponse create(
        CreateTeacherSubjectPermissionRequest request
    ) {
        if (
            permissionRepository.existsByTeacherIdAndSubjectId(
                request.teacherId(),
                request.subjectId()
            )
        ) {
            throw new ConflictException(
                "Permission already exists for teacherId=" +
                    request.teacherId() +
                    ", subjectId=" +
                    request.subjectId()
            );
        }
        validateScopesForAllPermissions(
            request.allPermissions(),
            request.scopes()
        );

        var subject = subjectRepository.getReferenceById(request.subjectId());
        var permission = TeacherSubjectPermission.builder()
            .teacher(
                teacherReferenceService.getTeacherReferenceById(
                    request.teacherId()
                )
            )
            .subject(subject)
            .allPermissions(request.allPermissions())
            .build();

        if (
            !request.allPermissions() &&
            request.scopes() != null &&
            !request.scopes().isEmpty()
        ) {
            permission
                .getScopes()
                .addAll(buildScopes(permission, request.scopes()));
        }

        return permissionMapper.toFullResponse(
            permissionRepository.save(permission)
        );
    }

    @Transactional
    @Override
    @PreAuthorize("@authz.canManagePermission(#id)")
    @CacheEvict(cacheNames = "userPermissions", allEntries = true)
    public TeacherSubjectPermissionResponse update(
        UUID id,
        UpdateTeacherSubjectPermissionRequest request
    ) {
        var permission = permissionRepository
            .findWithDetailsById(id)
            .orElseThrow(() ->
                new ResourceNotFoundException("TeacherSubjectPermission", id)
            );

        if (
            request.teacherId() != null &&
            !request.teacherId().equals(permission.getTeacher().getId())
        ) {
            if (
                permissionRepository.existsByTeacherIdAndSubjectId(
                    request.teacherId(),
                    permission.getSubject().getId()
                )
            ) {
                throw new ConflictException(
                    "Permission already exists for teacherId=" +
                        request.teacherId() +
                        ", subjectId=" +
                        permission.getSubject().getId()
                );
            }
            permission.setTeacher(
                teacherReferenceService.getTeacherReferenceById(
                    request.teacherId()
                )
            );
        }
        if (request.allPermissions() != null) {
            permission.setAllPermissions(request.allPermissions());
            if (request.allPermissions()) {
                permission.getScopes().clear();
            }
        }
        if (request.scopes() != null) {
            if (permission.isAllPermissions()) {
                throw new IllegalArgumentException(
                    "scopes must not be provided when allPermissions=true"
                );
            }
            if (request.scopes().isEmpty()) {
                throw new IllegalArgumentException(
                    "scopes must be non-empty when provided"
                );
            }
            var newScopes = buildScopes(permission, request.scopes());
            permission.getScopes().clear();
            permission.getScopes().addAll(newScopes);
        }

        return permissionMapper.toFullResponse(
            permissionRepository.save(permission)
        );
    }

    @Transactional
    @Override
    @PreAuthorize("@authz.canManagePermission(#id)")
    @CacheEvict(cacheNames = "userPermissions", allEntries = true)
    public void delete(UUID id) {
        var permission = permissionRepository
            .findById(id)
            .orElseThrow(() ->
                new ResourceNotFoundException("TeacherSubjectPermission", id)
            );
        permission.archive();
        permissionRepository.save(permission);
    }

    private void validateScopesForAllPermissions(
        boolean allPermissions,
        List<PermissionScopeRequest> scopes
    ) {
        if (!allPermissions && (scopes == null || scopes.isEmpty())) {
            throw new IllegalArgumentException(
                "scopes must be non-empty when allPermissions=false"
            );
        }
    }

    private List<PermissionScope> buildScopes(
        TeacherSubjectPermission permission,
        List<PermissionScopeRequest> requests
    ) {
        Subject subject = permission.getSubject();
        var subjectGroupIds = subject
            .getGroups()
            .stream()
            .map(Group::getId)
            .collect(java.util.stream.Collectors.toSet());

        var seen = new HashSet<String>();
        var result = new ArrayList<PermissionScope>();
        for (var req : requests) {
            Group group = null;
            Subgroup allowedSubgroup = null;
            UUID groupId = null;
            UUID subgroupId = null;
            if (req.group() != null) {
                groupId = req.group().groupId();
                subgroupId = req.group().allowedSubgroupId();
                if (!subjectGroupIds.contains(groupId)) {
                    throw new IllegalArgumentException(
                        "Group " +
                            groupId +
                            " is not attached to subject " +
                            subject.getId()
                    );
                }
                var ref = groupReferenceService.resolveAudience(
                    groupId,
                    subgroupId
                );
                group = ref.group();
                allowedSubgroup = ref.allowedSubgroup();
            }

            var key =
                groupId + "|" + subgroupId + "|" + req.allowedLessonType();
            if (!seen.add(key)) {
                throw new IllegalArgumentException(
                    "Duplicate scope in request: groupId=" +
                        groupId +
                        ", allowedSubgroupId=" +
                        subgroupId +
                        ", allowedLessonType=" +
                        req.allowedLessonType()
                );
            }
            result.add(
                PermissionScope.builder()
                    .permission(permission)
                    .group(group)
                    .allowedSubgroup(allowedSubgroup)
                    .allowedLessonType(req.allowedLessonType())
                    .build()
            );
        }
        return result;
    }
}
