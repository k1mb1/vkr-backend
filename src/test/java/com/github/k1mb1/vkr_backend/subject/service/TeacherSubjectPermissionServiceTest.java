package com.github.k1mb1.vkr_backend.subject.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.k1mb1.vkr_backend.common.exception.ConflictException;
import com.github.k1mb1.vkr_backend.common.exception.ResourceNotFoundException;
import com.github.k1mb1.vkr_backend.group.domain.GroupEntity;
import com.github.k1mb1.vkr_backend.subject.LessonType;
import com.github.k1mb1.vkr_backend.subject.domain.SubjectEntity;
import com.github.k1mb1.vkr_backend.subject.domain.TeacherSubjectPermissionEntity;
import com.github.k1mb1.vkr_backend.subject.mapper.TeacherSubjectPermissionMapper;
import com.github.k1mb1.vkr_backend.subject.repository.SubjectGroupRefRepository;
import com.github.k1mb1.vkr_backend.subject.repository.SubjectRepository;
import com.github.k1mb1.vkr_backend.subject.repository.SubjectSubgroupRefRepository;
import com.github.k1mb1.vkr_backend.subject.repository.SubjectTeacherRefRepository;
import com.github.k1mb1.vkr_backend.subject.repository.TeacherSubjectPermissionRepository;
import com.github.k1mb1.vkr_backend.subject.service.dto.request.CreateTeacherSubjectPermissionRequest;
import com.github.k1mb1.vkr_backend.subject.service.dto.request.PermissionScopeRequest;
import com.github.k1mb1.vkr_backend.subject.service.dto.request.PermissionScopeRequest.PermissionScopeGroupRef;
import com.github.k1mb1.vkr_backend.subject.service.dto.request.UpdateTeacherSubjectPermissionRequest;
import com.github.k1mb1.vkr_backend.subject.service.dto.response.TeacherSubjectPermissionResponse;
import com.github.k1mb1.vkr_backend.teacher.domain.TeacherEntity;
import jakarta.persistence.EntityNotFoundException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TeacherSubjectPermissionServiceTest {

    @Mock
    TeacherSubjectPermissionRepository permissionRepository;

    @Mock
    SubjectTeacherRefRepository teacherRefRepository;

    @Mock
    SubjectGroupRefRepository groupRefRepository;

    @Mock
    SubjectSubgroupRefRepository subgroupRefRepository;

    @Mock
    SubjectRepository subjectRepository;

    @Mock
    TeacherSubjectPermissionMapper permissionMapper;

    @InjectMocks
    TeacherSubjectPermissionService service;

    // ---- create ----

    @Test
    void createThrowsConflictWhenPermissionExists() {
        var teacherId = UUID.randomUUID();
        var subjectId = UUID.randomUUID();
        when(permissionRepository.existsByTeacherIdAndSubjectId(teacherId, subjectId))
                .thenReturn(true);

        assertThatThrownBy(() ->
                        service.create(new CreateTeacherSubjectPermissionRequest(teacherId, subjectId, true, null)))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void createRejectsNonAllPermissionsWithoutScopes() {
        var teacherId = UUID.randomUUID();
        var subjectId = UUID.randomUUID();
        when(permissionRepository.existsByTeacherIdAndSubjectId(teacherId, subjectId))
                .thenReturn(false);

        assertThatThrownBy(() -> service.create(
                        new CreateTeacherSubjectPermissionRequest(teacherId, subjectId, false, List.of())))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("scopes must be non-empty");
    }

    @Test
    void createAllPermissionsSavesPermission() {
        var teacherId = UUID.randomUUID();
        var subjectId = UUID.randomUUID();
        when(permissionRepository.existsByTeacherIdAndSubjectId(teacherId, subjectId))
                .thenReturn(false);
        when(subjectRepository.getReferenceById(subjectId))
                .thenReturn(SubjectEntity.builder().id(subjectId).name("S").build());
        when(teacherRefRepository.getReferenceById(teacherId))
                .thenReturn(TeacherEntity.builder().id(teacherId).build());
        when(permissionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(permissionMapper.toFullResponse(any())).thenReturn(mock(TeacherSubjectPermissionResponse.class));

        service.create(new CreateTeacherSubjectPermissionRequest(teacherId, subjectId, true, null));

        verify(permissionRepository).save(any(TeacherSubjectPermissionEntity.class));
    }

    @Test
    void createRejectsScopeWithGroupNotAttachedToSubject() {
        var teacherId = UUID.randomUUID();
        var subjectId = UUID.randomUUID();
        var foreignGroupId = UUID.randomUUID();
        when(permissionRepository.existsByTeacherIdAndSubjectId(teacherId, subjectId))
                .thenReturn(false);
        // предмет без групп — любой groupId в scope считается не привязанным
        when(subjectRepository.getReferenceById(subjectId))
                .thenReturn(SubjectEntity.builder()
                        .id(subjectId)
                        .name("S")
                        .groups(new HashSet<>())
                        .build());
        when(teacherRefRepository.getReferenceById(teacherId))
                .thenReturn(TeacherEntity.builder().id(teacherId).build());

        var scope = new PermissionScopeRequest(new PermissionScopeGroupRef(foreignGroupId, null), LessonType.LECTURE);

        assertThatThrownBy(() -> service.create(
                        new CreateTeacherSubjectPermissionRequest(teacherId, subjectId, false, List.of(scope))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not attached to subject");
    }

    @Test
    void createRejectsDuplicateScopes() {
        var teacherId = UUID.randomUUID();
        var subjectId = UUID.randomUUID();
        var groupId = UUID.randomUUID();
        when(permissionRepository.existsByTeacherIdAndSubjectId(teacherId, subjectId))
                .thenReturn(false);
        var group = GroupEntity.builder().id(groupId).name("G").build();
        when(subjectRepository.getReferenceById(subjectId))
                .thenReturn(SubjectEntity.builder()
                        .id(subjectId)
                        .name("S")
                        .groups(new HashSet<>(Set.of(group)))
                        .build());
        when(teacherRefRepository.getReferenceById(teacherId))
                .thenReturn(TeacherEntity.builder().id(teacherId).build());
        lenient().when(groupRefRepository.getReferenceById(groupId)).thenReturn(group);
        lenient()
                .when(subgroupRefRepository.resolveAllowedSubgroup(null, groupId))
                .thenReturn(null);

        var scope = new PermissionScopeRequest(new PermissionScopeGroupRef(groupId, null), LessonType.LECTURE);

        assertThatThrownBy(() -> service.create(
                        new CreateTeacherSubjectPermissionRequest(teacherId, subjectId, false, List.of(scope, scope))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Duplicate scope");
    }

    // ---- update ----

    @Test
    void updateThrowsWhenMissing() {
        var id = UUID.randomUUID();
        when(permissionRepository.findWithDetailsById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(id, new UpdateTeacherSubjectPermissionRequest(null, null, null)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateRejectsScopesWhenAllPermissions() {
        var id = UUID.randomUUID();
        var permission = TeacherSubjectPermissionEntity.builder()
                .id(id)
                .allPermissions(true)
                .teacher(TeacherEntity.builder().id(UUID.randomUUID()).build())
                .subject(SubjectEntity.builder().id(UUID.randomUUID()).name("S").build())
                .build();
        when(permissionRepository.findWithDetailsById(id)).thenReturn(Optional.of(permission));

        var scope = new PermissionScopeRequest(null, LessonType.LECTURE);

        assertThatThrownBy(
                        () -> service.update(id, new UpdateTeacherSubjectPermissionRequest(null, null, List.of(scope))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must not be provided when allPermissions=true");
    }

    // ---- delete ----

    @Test
    void deleteArchivesPermission() {
        var id = UUID.randomUUID();
        var permission = TeacherSubjectPermissionEntity.builder().id(id).build();
        when(permissionRepository.findById(id)).thenReturn(Optional.of(permission));

        service.delete(id);

        assertThat(permission.isArchived()).isTrue();
        verify(permissionRepository).save(permission);
    }

    @Test
    void deleteThrowsWhenMissing() {
        var id = UUID.randomUUID();
        when(permissionRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(id)).isInstanceOf(ResourceNotFoundException.class);
    }

    // ---- getPermission ----

    @Test
    void getPermissionThrowsWhenMissing() {
        var subjectId = UUID.randomUUID();
        var teacherId = UUID.randomUUID();
        when(permissionRepository.findBySubjectIdAndTeacherId(subjectId, teacherId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getPermission(subjectId, teacherId))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
