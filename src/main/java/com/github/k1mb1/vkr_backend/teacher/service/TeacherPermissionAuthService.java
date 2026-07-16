package com.github.k1mb1.vkr_backend.teacher.service;

import com.github.k1mb1.vkr_backend.auth.api.PermissionAuthPort;
import com.github.k1mb1.vkr_backend.auth.api.PermissionGrantResponse;
import com.github.k1mb1.vkr_backend.teacher.repository.TeacherSubjectPermissionRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Реализация {@link PermissionAuthPort}: teacher владеет выданными правами
 * и отдаёт auth лёгкие проекции для авторизационного снапшота.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeacherPermissionAuthService implements PermissionAuthPort {

    private final TeacherSubjectPermissionRepository permissionRepository;

    @Override
    public List<PermissionGrantResponse> grantsOfTeacher(UUID teacherId) {
        return permissionRepository.findOwnedByTeacherId(teacherId).stream()
                .map(row ->
                        new PermissionGrantResponse(row.getPermissionId(), row.getSubjectId(), row.getAllPermissions()))
                .toList();
    }

    @Override
    public Optional<UUID> subjectIdOfPermission(UUID permissionId) {
        return permissionRepository.findSubjectIdById(permissionId);
    }
}
