package com.github.k1mb1.vkr_backend.subject.internal;

import com.github.k1mb1.vkr_backend.group.GroupReferenceService;
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
    public List<TeacherSubjectPermissionResponse> getPermissions(
        UUID subjectId
    ) {
        return permissionRepository
            .findBySubjectIdFetchDetails(subjectId)
            .stream()
            .map(permissionMapper::toResponse)
            .toList();
    }

    @Transactional
    @Override
    public TeacherSubjectPermissionResponse create(
        CreateTeacherSubjectPermissionRequest request
    ) {
        var permission = TeacherSubjectPermission.builder()
            .teacher(
                teacherReferenceService.getTeacherReferenceById(
                    request.teacherId()
                )
            )
            .subject(subjectRepository.getReferenceById(request.subjectId()))
            .group(
                groupReferenceService.getGroupReferenceById(request.groupId())
            )
            .allowedSubgroup(
                request.allowedSubgroupId() != null
                    ? groupReferenceService.getSubgroupReferenceById(
                          request.allowedSubgroupId()
                      )
                    : null
            )
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

        permissionMapper.updateEntity(request, permission);

        if (request.teacherId() != null) {
            permission.setTeacher(
                teacherReferenceService.getTeacherReferenceById(
                    request.teacherId()
                )
            );
        }
        if (request.groupId() != null) {
            permission.setGroup(
                groupReferenceService.getGroupReferenceById(request.groupId())
            );
        }
        if (request.allowedSubgroupId() != null) {
            permission.setAllowedSubgroup(
                groupReferenceService.getSubgroupReferenceById(
                    request.allowedSubgroupId()
                )
            );
        } else if (
            request.allowedSubgroupId() == null &&
            permission.getAllowedSubgroup() != null
        ) {
            permission.setAllowedSubgroup(null);
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
}
