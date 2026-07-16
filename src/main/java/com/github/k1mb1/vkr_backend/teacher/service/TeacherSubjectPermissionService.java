package com.github.k1mb1.vkr_backend.teacher.service;

import com.github.k1mb1.vkr_backend.common.exception.ConflictException;
import com.github.k1mb1.vkr_backend.common.exception.ResourceNotFoundException;
import com.github.k1mb1.vkr_backend.group.domain.GroupEntity;
import com.github.k1mb1.vkr_backend.group.domain.SubgroupEntity;
import com.github.k1mb1.vkr_backend.subject.domain.SubjectEntity;
import com.github.k1mb1.vkr_backend.teacher.domain.PermissionScopeEntity;
import com.github.k1mb1.vkr_backend.teacher.domain.TeacherSubjectPermissionEntity;
import com.github.k1mb1.vkr_backend.teacher.mapper.TeacherSubjectPermissionMapper;
import com.github.k1mb1.vkr_backend.teacher.repository.TeacherGroupRefRepository;
import com.github.k1mb1.vkr_backend.teacher.repository.TeacherRepository;
import com.github.k1mb1.vkr_backend.teacher.repository.TeacherSubgroupRefRepository;
import com.github.k1mb1.vkr_backend.teacher.repository.TeacherSubjectPermissionRepository;
import com.github.k1mb1.vkr_backend.teacher.repository.TeacherSubjectRefRepository;
import com.github.k1mb1.vkr_backend.teacher.service.dto.request.CreateTeacherSubjectPermissionRequest;
import com.github.k1mb1.vkr_backend.teacher.service.dto.request.PermissionScopeRequest;
import com.github.k1mb1.vkr_backend.teacher.service.dto.request.UpdateTeacherSubjectPermissionRequest;
import com.github.k1mb1.vkr_backend.teacher.service.dto.response.TeacherSubjectPermissionResponse;
import jakarta.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeacherSubjectPermissionService {

    final TeacherSubjectPermissionRepository permissionRepository;

    final TeacherRepository teacherRefRepository;

    final TeacherGroupRefRepository groupRefRepository;

    final TeacherSubgroupRefRepository subgroupRefRepository;

    final TeacherSubjectRefRepository subjectRepository;

    final TeacherSubjectPermissionMapper permissionMapper;

    @PreAuthorize("@authz.canManageSubject(#subjectId)")
    public List<TeacherSubjectPermissionResponse> getPermissionsBySubject(UUID subjectId) {
        return permissionRepository.findAllBySubjectId(subjectId).stream()
                .map(permissionMapper::toFullResponse)
                .toList();
    }

    @PreAuthorize("@authz.isSelfOrAdmin(#teacherId) or @authz.canManageSubject(#subjectId)")
    public TeacherSubjectPermissionResponse getPermission(UUID subjectId, UUID teacherId) {
        return permissionRepository
                .findBySubjectIdAndTeacherId(subjectId, teacherId)
                .map(permissionMapper::toFullResponse)
                .orElseThrow(() -> new EntityNotFoundException(
                        "TeacherSubjectPermission not found for subjectId=" + subjectId + ", teacherId=" + teacherId));
    }

    @Transactional
    @PreAuthorize("@authz.canManageSubject(#request.subjectId())")
    @CacheEvict(cacheNames = "userPermissions", allEntries = true)
    public TeacherSubjectPermissionResponse create(CreateTeacherSubjectPermissionRequest request) {
        if (permissionRepository.existsByTeacherIdAndSubjectId(request.teacherId(), request.subjectId())) {
            throw new ConflictException("Permission already exists for teacherId=" + request.teacherId()
                    + ", subjectId="
                    + request.subjectId());
        }
        validateScopesForAllPermissions(request.allPermissions(), request.scopes());

        var subject = subjectRepository.getReferenceById(request.subjectId());
        var permission = TeacherSubjectPermissionEntity.builder()
                .teacher(teacherRefRepository.getReferenceById(request.teacherId()))
                .subject(subject)
                .allPermissions(request.allPermissions())
                .build();

        if (!request.allPermissions()
                && request.scopes() != null
                && !request.scopes().isEmpty()) {
            permission.getScopes().addAll(buildScopes(permission, request.scopes()));
        }

        return permissionMapper.toFullResponse(permissionRepository.save(permission));
    }

    @Transactional
    @PreAuthorize("@authz.canManagePermission(#id)")
    @CacheEvict(cacheNames = "userPermissions", allEntries = true)
    public TeacherSubjectPermissionResponse update(UUID id, UpdateTeacherSubjectPermissionRequest request) {
        var permission = permissionRepository
                .findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TeacherSubjectPermission", id));

        if (request.teacherId() != null
                && !request.teacherId().equals(permission.getTeacher().getId())) {
            if (permissionRepository.existsByTeacherIdAndSubjectId(
                    request.teacherId(), permission.getSubject().getId())) {
                throw new ConflictException("Permission already exists for teacherId=" + request.teacherId()
                        + ", subjectId="
                        + permission.getSubject().getId());
            }
            permission.setTeacher(teacherRefRepository.getReferenceById(request.teacherId()));
        }
        if (request.allPermissions() != null) {
            permission.setAllPermissions(request.allPermissions());
            if (request.allPermissions()) {
                permission.getScopes().clear();
            }
        }
        if (request.scopes() != null) {
            if (permission.isAllPermissions()) {
                throw new IllegalArgumentException("scopes must not be provided when allPermissions=true");
            }
            if (request.scopes().isEmpty()) {
                throw new IllegalArgumentException("scopes must be non-empty when provided");
            }
            var newScopes = buildScopes(permission, request.scopes());
            permission.getScopes().clear();
            permission.getScopes().addAll(newScopes);
        }

        return permissionMapper.toFullResponse(permissionRepository.save(permission));
    }

    @Transactional
    @PreAuthorize("@authz.canManagePermission(#id)")
    @CacheEvict(cacheNames = "userPermissions", allEntries = true)
    public void delete(UUID id) {
        var permission = permissionRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TeacherSubjectPermission", id));
        permission.archive();
        permissionRepository.save(permission);
    }

    private void validateScopesForAllPermissions(boolean allPermissions, List<PermissionScopeRequest> scopes) {
        if (!allPermissions && (scopes == null || scopes.isEmpty())) {
            throw new IllegalArgumentException("scopes must be non-empty when allPermissions=false");
        }
    }

    private List<PermissionScopeEntity> buildScopes(
            TeacherSubjectPermissionEntity permission, List<PermissionScopeRequest> requests) {
        SubjectEntity subject = permission.getSubject();
        var subjectGroupIds =
                subject.getGroups().stream().map(GroupEntity::getId).collect(java.util.stream.Collectors.toSet());

        var seen = new HashSet<String>();
        var result = new ArrayList<PermissionScopeEntity>();
        for (var req : requests) {
            GroupEntity group = null;
            SubgroupEntity allowedSubgroup = null;
            UUID groupId = null;
            UUID subgroupId = null;
            if (req.group() != null) {
                groupId = req.group().groupId();
                subgroupId = req.group().allowedSubgroupId();
                if (!subjectGroupIds.contains(groupId)) {
                    throw new IllegalArgumentException(
                            "Group " + groupId + " is not attached to subject " + subject.getId());
                }
                group = groupRefRepository.getReferenceById(groupId);
                allowedSubgroup = subgroupRefRepository.resolveAllowedSubgroup(subgroupId, groupId);
            }

            var key = groupId + "|" + subgroupId + "|" + req.allowedLessonType();
            if (!seen.add(key)) {
                throw new IllegalArgumentException("Duplicate scope in request: groupId=" + groupId
                        + ", allowedSubgroupId="
                        + subgroupId
                        + ", allowedLessonType="
                        + req.allowedLessonType());
            }
            result.add(PermissionScopeEntity.builder()
                    .permission(permission)
                    .group(group)
                    .allowedSubgroup(allowedSubgroup)
                    .allowedLessonType(req.allowedLessonType())
                    .build());
        }
        return result;
    }
}
