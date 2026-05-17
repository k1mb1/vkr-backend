package com.github.k1mb1.vkr_backend.subject.internal;

import com.github.k1mb1.vkr_backend.group.GroupReferenceService;
import com.github.k1mb1.vkr_backend.group.domain.Group;
import com.github.k1mb1.vkr_backend.group.domain.Subgroup;
import com.github.k1mb1.vkr_backend.subject.TeacherSubjectPermissionsApi;
import com.github.k1mb1.vkr_backend.subject.domain.TeacherSubjectPermission;
import com.github.k1mb1.vkr_backend.subject.web.requests.CreateTeacherSubjectPermissionRequest;
import com.github.k1mb1.vkr_backend.subject.web.requests.UpdateTeacherSubjectPermissionRequest;
import com.github.k1mb1.vkr_backend.subject.web.responses.TeacherSubjectPermissionResponse;
import com.github.k1mb1.vkr_backend.teacher.TeacherReferenceService;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
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
    public List<TeacherSubjectPermissionResponse> getPermissionsBySubject(
        UUID subjectId
    ) {
        return permissionRepository
            .findBySubjectIdFetchDetails(subjectId)
            .stream()
            .map(permissionMapper::toResponse)
            .toList();
    }

    @Override
    public TeacherSubjectPermissionResponse getPermission(
        UUID subjectId,
        UUID teacherId
    ) {
        return permissionRepository
            .findBySubjectIdAndTeacherIdFetchDetails(subjectId, teacherId)
            .map(permissionMapper::toResponse)
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
    public TeacherSubjectPermissionResponse create(
        CreateTeacherSubjectPermissionRequest request
    ) {
        if (
            permissionRepository.existsByTeacherIdAndSubjectId(
                request.teacherId(),
                request.subjectId()
            )
        ) {
            throw new IllegalStateException(
                "Permission already exists for teacherId=" +
                    request.teacherId() +
                    ", subjectId=" +
                    request.subjectId()
            );
        }

        var teacher = teacherReferenceService.getTeacherReferenceById(
            request.teacherId()
        );
        var subject = subjectRepository.getReferenceById(request.subjectId());
        var group = groupReferenceService.getGroupReferenceById(
            request.groupId()
        );
        var allowedSubgroup =
            request.allowedSubgroupId() != null
                ? groupReferenceService.getSubgroupReferenceById(
                      request.allowedSubgroupId()
                  )
                : null;

        validateSubgroupBelongsToGroup(allowedSubgroup, group);

        var permission = TeacherSubjectPermission.builder()
            .teacher(teacher)
            .subject(subject)
            .group(group)
            .allowedSubgroup(allowedSubgroup)
            .allowedLessonType(request.allowedLessonType())
            .build();
        return permissionMapper.toResponse(
            permissionRepository.save(permission)
        );
    }

    @Transactional
    @Override
    public TeacherSubjectPermissionResponse update(
        UUID id,
        UpdateTeacherSubjectPermissionRequest request
    ) {
        var permission = permissionRepository
            .findById(id)
            .orElseThrow(() ->
                new EntityNotFoundException(
                    "TeacherSubjectPermission not found: " + id
                )
            );

        if (
            request.teacherId() != null &&
            !request.teacherId().equals(permission.getTeacher().getId()) &&
            permissionRepository.existsByTeacherIdAndSubjectId(
                request.teacherId(),
                permission.getSubject().getId()
            )
        ) {
            throw new IllegalStateException(
                "Permission already exists for teacherId=" +
                    request.teacherId() +
                    ", subjectId=" +
                    permission.getSubject().getId()
            );
        }

        var newGroup =
            request.groupId() != null
                ? groupReferenceService.getGroupReferenceById(request.groupId())
                : permission.getGroup();
        var newSubgroup =
            request.allowedSubgroupId() != null
                ? groupReferenceService.getSubgroupReferenceById(
                      request.allowedSubgroupId()
                  )
                : permission.getAllowedSubgroup();

        validateSubgroupBelongsToGroup(newSubgroup, newGroup);

        permissionMapper.updateEntity(request, permission);

        if (request.teacherId() != null) {
            permission.setTeacher(
                teacherReferenceService.getTeacherReferenceById(
                    request.teacherId()
                )
            );
        }
        if (request.groupId() != null) {
            permission.setGroup(newGroup);
        }
        if (request.allowedSubgroupId() != null) {
            permission.setAllowedSubgroup(newSubgroup);
        }

        return permissionMapper.toResponse(
            permissionRepository.save(permission)
        );
    }

    @Transactional
    @Override
    public void delete(UUID id) {
        var permission = permissionRepository
            .findById(id)
            .orElseThrow(() ->
                new EntityNotFoundException(
                    "TeacherSubjectPermission not found: " + id
                )
            );
        permission.archive();
        permissionRepository.save(permission);
    }

    private void validateSubgroupBelongsToGroup(
        Subgroup subgroup,
        Group group
    ) {
        if (
            subgroup != null &&
            !subgroup.getGroup().getId().equals(group.getId())
        ) {
            throw new IllegalArgumentException(
                "Subgroup does not belong to the specified group"
            );
        }
    }
}
