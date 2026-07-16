package com.github.k1mb1.vkr_backend.teacher.service;

import com.github.k1mb1.vkr_backend.subject.api.OwnerPermissionGranter;
import com.github.k1mb1.vkr_backend.subject.api.SubjectVisibilityPort;
import com.github.k1mb1.vkr_backend.teacher.domain.TeacherSubjectPermissionEntity;
import com.github.k1mb1.vkr_backend.teacher.repository.TeacherRepository;
import com.github.k1mb1.vkr_backend.teacher.repository.TeacherSubjectPermissionRepository;
import com.github.k1mb1.vkr_backend.teacher.repository.TeacherSubjectRefRepository;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Реализация портов subject ({@link OwnerPermissionGranter}, {@link SubjectVisibilityPort}):
 * teacher владеет правами и отдаёт subject'у то, что тому нужно про доступ преподавателей,
 * не давая subject зависеть от teacher (инверсия — граф остаётся ацикличным).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeacherSubjectAccessService implements OwnerPermissionGranter, SubjectVisibilityPort {

    private final TeacherSubjectPermissionRepository permissionRepository;

    private final TeacherRepository teacherRefRepository;

    private final TeacherSubjectRefRepository subjectRefRepository;

    @Override
    @Transactional
    public void grantAllPermissions(UUID teacherId, UUID subjectId) {
        var permission = TeacherSubjectPermissionEntity.builder()
                .teacher(teacherRefRepository.getReferenceById(teacherId))
                .subject(subjectRefRepository.getReferenceById(subjectId))
                .allPermissions(true)
                .build();
        permissionRepository.save(permission);
    }

    @Override
    public Set<UUID> visibleSubjectIds(UUID teacherId) {
        return Set.copyOf(permissionRepository.findSubjectIdsByTeacherId(teacherId));
    }
}
