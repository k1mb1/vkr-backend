package com.github.k1mb1.vkr_backend.subject.internal;

import com.github.k1mb1.vkr_backend.group.GroupReferenceService;
import com.github.k1mb1.vkr_backend.subject.TeacherSubjectPermissionsApi;
import com.github.k1mb1.vkr_backend.subject.domain.TeacherSubjectPermission;
import com.github.k1mb1.vkr_backend.subject.web.requests.CreateTeacherSubjectPermissionRequest;
import com.github.k1mb1.vkr_backend.subject.web.requests.UpdateTeacherSubjectPermissionRequest;
import com.github.k1mb1.vkr_backend.subject.web.responses.TeacherSubjectPermissionResponse;
import com.github.k1mb1.vkr_backend.teacher.TeacherReferenceService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class TeacherSubjectPermissionService
    implements TeacherSubjectPermissionsApi {

    final TeacherSubjectPermissionRepository permissionRepository;

    final TeacherReferenceService teacherReferenceService;

    final GroupReferenceService groupReferenceService;

    final SubjectRepository subjectRepository;

    final TeacherSubjectPermissionMapper permissionMapper;

    @Override
    public List<TeacherSubjectPermissionResponse> getPermissions(
        UUID subjectId
    ) {
        return permissionRepository.findBySubjectIdFetchDetails(subjectId)
            .stream()
            .map(permissionMapper::toResponse)
            .toList();
    }

    @Transactional
    @Override
    public TeacherSubjectPermissionResponse create(
        CreateTeacherSubjectPermissionRequest request
    ) {
        var teacher = teacherReferenceService.getTeacherReferenceById(request.teacherId());
        var subject = subjectRepository.getReferenceById(request.subjectId());
        var group = groupReferenceService.getGroupReferenceById(request.groupId());
        var allowedSubgroup = request.allowedSubgroupId() != null
                              ? groupReferenceService.getSubgroupReferenceById(request.allowedSubgroupId())
                              : null;

        validateSubgroupBelongsToGroup(allowedSubgroup, group);
        validateUniqueCombination(
            request.teacherId(),
            request.subjectId(),
            request.groupId(),
            request.allowedSubgroupId(),
            request.allowedLessonType()
        );

        var permission = TeacherSubjectPermission.builder()
            .teacher(teacher)
            .subject(subject)
            .group(group)
            .allowedSubgroup(allowedSubgroup)
            .allowedLessonType(request.allowedLessonType())
            .build();

        return permissionMapper.toResponse(permissionRepository.save(permission));
    }

    @Transactional
    @Override
    public TeacherSubjectPermissionResponse update(
        UUID id,
        UpdateTeacherSubjectPermissionRequest request
    ) {
        var permission = permissionRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("TeacherSubjectPermission not found: " + id));

        var newGroup = request.groupId() != null
                       ? groupReferenceService.getGroupReferenceById(request.groupId())
                       : permission.getGroup();
        var newSubgroup = request.allowedSubgroupId() != null
                          ? groupReferenceService.getSubgroupReferenceById(request.allowedSubgroupId())
                          : (request.allowedSubgroupId() == null && permission.getAllowedSubgroup() != null)
                            ? null
                            : permission.getAllowedSubgroup();

        validateSubgroupBelongsToGroup(newSubgroup, newGroup);

        UUID newTeacherId = request.teacherId() != null
                            ? request.teacherId()
                            : permission.getTeacher().getId();
        UUID newSubjectId = permission.getSubject().getId();
        UUID newGroupId = newGroup.getId();
        UUID newSubgroupId = newSubgroup != null
                             ? newSubgroup.getId()
                             : null;
        var newLessonType = request.allowedLessonType() != null
                            ? request.allowedLessonType()
                            : permission.getAllowedLessonType();

        if (!newTeacherId.equals(permission.getTeacher()
                                     .getId()) || !newGroupId.equals(permission.getGroup()
                                                                         .getId()) || !java.util.Objects.equals(
            newSubgroupId,
            permission.getAllowedSubgroup() != null
            ? permission.getAllowedSubgroup().getId()
            : null
        ) || !java.util.Objects.equals(newLessonType, permission.getAllowedLessonType())) {
            validateUniqueCombination(
                newTeacherId,
                newSubjectId,
                newGroupId,
                newSubgroupId,
                newLessonType
            );
        }

        permissionMapper.updateEntity(request, permission);

        if (request.teacherId() != null) {
            permission.setTeacher(teacherReferenceService.getTeacherReferenceById(request.teacherId()));
        }
        if (request.groupId() != null) {
            permission.setGroup(newGroup);
        }
        permission.setAllowedSubgroup(newSubgroup);

        return permissionMapper.toResponse(permissionRepository.save(permission));
    }

    @Transactional
    @Override
    public void delete(UUID id) {
        var permission = permissionRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("TeacherSubjectPermission not found: " + id));
        permission.archive();
        permissionRepository.save(permission);
    }

    private void validateSubgroupBelongsToGroup(
        com.github.k1mb1.vkr_backend.group.domain.Subgroup subgroup,
        com.github.k1mb1.vkr_backend.group.domain.Group group
    ) {
        if (subgroup != null && !subgroup.getGroup().getId().equals(group.getId())) {
            throw new IllegalArgumentException("Subgroup does not belong to the specified group");
        }
    }

    private void validateUniqueCombination(
        UUID teacherId,
        UUID subjectId,
        UUID groupId,
        UUID subgroupId,
        com.github.k1mb1.vkr_backend.lesson.domain.LessonType lessonType
    ) {
        if (permissionRepository.existsActiveByUniqueCombination(
            teacherId,
            subjectId,
            groupId,
            subgroupId,
            lessonType
        )) {
            throw new IllegalStateException("Permission already exists for this combination");
        }
    }
}
