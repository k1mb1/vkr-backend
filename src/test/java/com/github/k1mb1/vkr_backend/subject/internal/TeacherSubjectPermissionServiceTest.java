package com.github.k1mb1.vkr_backend.subject.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.k1mb1.vkr_backend.common.error.ResourceNotFoundException;
import com.github.k1mb1.vkr_backend.group.GroupReferenceService;
import com.github.k1mb1.vkr_backend.subject.domain.Subject;
import com.github.k1mb1.vkr_backend.subject.domain.TeacherSubjectPermission;
import com.github.k1mb1.vkr_backend.subject.web.requests.CreateTeacherSubjectPermissionRequest;
import com.github.k1mb1.vkr_backend.subject.web.requests.UpdateTeacherSubjectPermissionRequest;
import com.github.k1mb1.vkr_backend.subject.web.responses.TeacherSubjectPermissionResponse;
import com.github.k1mb1.vkr_backend.teacher.TeacherReferenceService;
import com.github.k1mb1.vkr_backend.teacher.domain.Teacher;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
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
    TeacherReferenceService teacherReferenceService;

    @Mock
    GroupReferenceService groupReferenceService;

    @Mock
    SubjectRepository subjectRepository;

    @Mock
    TeacherSubjectPermissionMapper permissionMapper;

    @InjectMocks
    TeacherSubjectPermissionService service;

    private final UUID permissionId = UUID.randomUUID();
    private final UUID subjectId = UUID.randomUUID();
    private final UUID teacherId = UUID.randomUUID();

    // ------------------------------------------------------------------ getPermissionsBySubject

    @Test
    void getPermissionsBySubjectReturnsMappedList() {
        var permission = permissionWithAllPermissions(true);
        var response = buildResponse(permissionId);

        when(permissionRepository.findAllBySubjectId(subjectId)).thenReturn(List.of(permission));
        when(permissionMapper.toResponse(permission)).thenReturn(response);
        when(permissionMapper.scopesForPermission(permission)).thenReturn(List.of());

        var result = service.getPermissionsBySubject(subjectId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(permissionId);
    }

    // ------------------------------------------------------------------ getPermission

    @Test
    void getPermissionThrowsWhenNotFound() {
        when(permissionRepository.findBySubjectIdAndTeacherId(subjectId, teacherId))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getPermission(subjectId, teacherId))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("TeacherSubjectPermission");
    }

    @Test
    void getPermissionReturnsMappedPermission() {
        var permission = permissionWithAllPermissions(true);
        var response = buildResponse(permissionId);

        when(permissionRepository.findBySubjectIdAndTeacherId(subjectId, teacherId))
            .thenReturn(Optional.of(permission));
        when(permissionMapper.toResponse(permission)).thenReturn(response);
        when(permissionMapper.scopesForPermission(permission)).thenReturn(List.of());

        var result = service.getPermission(subjectId, teacherId);

        assertThat(result.id()).isEqualTo(permissionId);
    }

    // ------------------------------------------------------------------ create

    @Test
    void createThrowsWhenPermissionAlreadyExists() {
        when(permissionRepository.existsByTeacherIdAndSubjectId(teacherId, subjectId))
            .thenReturn(true);

        var request = new CreateTeacherSubjectPermissionRequest(teacherId, subjectId, true, null);

        assertThatThrownBy(() -> service.create(request))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Permission already exists");

        verify(permissionRepository, never()).save(any());
    }

    @Test
    void createThrowsWhenNotAllPermissionsAndNoScopes() {
        when(permissionRepository.existsByTeacherIdAndSubjectId(teacherId, subjectId))
            .thenReturn(false);

        var request = new CreateTeacherSubjectPermissionRequest(teacherId, subjectId, false, null);

        assertThatThrownBy(() -> service.create(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("scopes must be non-empty");

        verify(permissionRepository, never()).save(any());
    }

    @Test
    void createWithAllPermissionsSucceeds() {
        var teacher = Teacher.builder().id(teacherId).build();
        var subject = Subject.builder().name("Math").build();
        var savedPermission = TeacherSubjectPermission.builder()
            .teacher(teacher)
            .subject(subject)
            .allPermissions(true)
            .build();
        var response = buildResponse(permissionId);

        when(permissionRepository.existsByTeacherIdAndSubjectId(teacherId, subjectId))
            .thenReturn(false);
        when(teacherReferenceService.getTeacherReferenceById(teacherId)).thenReturn(teacher);
        when(subjectRepository.getReferenceById(subjectId)).thenReturn(subject);
        when(permissionRepository.save(any())).thenReturn(savedPermission);
        when(permissionMapper.toResponse(savedPermission)).thenReturn(response);
        when(permissionMapper.scopesForPermission(savedPermission)).thenReturn(List.of());

        var request = new CreateTeacherSubjectPermissionRequest(teacherId, subjectId, true, null);
        var result = service.create(request);

        assertThat(result.id()).isEqualTo(permissionId);
        verify(permissionRepository).save(any());
    }

    // ------------------------------------------------------------------ update

    @Test
    void updateThrowsWhenPermissionNotFound() {
        when(permissionRepository.findWithDetailsById(permissionId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(permissionId,
            new UpdateTeacherSubjectPermissionRequest(null, null, null)))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("TeacherSubjectPermission");
    }

    @Test
    void updateThrowsScopesWithAllPermissionsTrue() {
        var teacher = Teacher.builder().id(teacherId).build();
        var subject = Subject.builder().name("Math").build();
        var permission = TeacherSubjectPermission.builder()
            .teacher(teacher)
            .subject(subject)
            .allPermissions(true)
            .build();

        when(permissionRepository.findWithDetailsById(permissionId))
            .thenReturn(Optional.of(permission));

        // providing scopes when allPermissions is already true
        var scopeRequest = new com.github.k1mb1.vkr_backend.subject.web.requests.PermissionScopeRequest(null, null);
        var request = new UpdateTeacherSubjectPermissionRequest(null, null, List.of(scopeRequest));

        assertThatThrownBy(() -> service.update(permissionId, request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("allPermissions=true");
    }

    @Test
    void updateThrowsWhenScopesProvidedAsEmpty() {
        var teacher = Teacher.builder().id(teacherId).build();
        var subject = Subject.builder().name("Math").build();
        var permission = TeacherSubjectPermission.builder()
            .teacher(teacher)
            .subject(subject)
            .allPermissions(false)
            .build();

        when(permissionRepository.findWithDetailsById(permissionId))
            .thenReturn(Optional.of(permission));

        // empty scopes list is not allowed
        var request = new UpdateTeacherSubjectPermissionRequest(null, null, List.of());

        assertThatThrownBy(() -> service.update(permissionId, request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("scopes must be non-empty");
    }

    @Test
    void updateWithNoChangesSucceeds() {
        var teacher = Teacher.builder().id(teacherId).build();
        var subject = Subject.builder().name("Math").build();
        var permission = TeacherSubjectPermission.builder()
            .teacher(teacher)
            .subject(subject)
            .allPermissions(true)
            .build();
        var response = buildResponse(permissionId);

        when(permissionRepository.findWithDetailsById(permissionId))
            .thenReturn(Optional.of(permission));
        when(permissionRepository.save(permission)).thenReturn(permission);
        when(permissionMapper.toResponse(permission)).thenReturn(response);
        when(permissionMapper.scopesForPermission(permission)).thenReturn(List.of());

        var request = new UpdateTeacherSubjectPermissionRequest(null, null, null);
        var result = service.update(permissionId, request);

        assertThat(result.id()).isEqualTo(permissionId);
        verify(permissionRepository).save(permission);
    }

    // ------------------------------------------------------------------ delete

    @Test
    void deleteThrowsWhenPermissionNotFound() {
        when(permissionRepository.findById(permissionId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(permissionId))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("TeacherSubjectPermission");
    }

    @Test
    void deleteArchivesPermission() {
        var permission = TeacherSubjectPermission.builder()
            .teacher(Teacher.builder().id(teacherId).build())
            .subject(Subject.builder().name("Math").build())
            .allPermissions(true)
            .build();

        when(permissionRepository.findById(permissionId)).thenReturn(Optional.of(permission));
        when(permissionRepository.save(permission)).thenReturn(permission);

        service.delete(permissionId);

        assertThat(permission.isArchived()).isTrue();
        verify(permissionRepository).save(permission);
    }

    // ------------------------------------------------------------------ helpers

    private TeacherSubjectPermission permissionWithAllPermissions(boolean allPermissions) {
        var teacher = Teacher.builder().id(teacherId).build();
        var subject = Subject.builder().name("Math").build();
        return TeacherSubjectPermission.builder()
            .teacher(teacher)
            .subject(subject)
            .allPermissions(allPermissions)
            .build();
    }

    private TeacherSubjectPermissionResponse buildResponse(UUID id) {
        return new TeacherSubjectPermissionResponse(id, teacherId, "teacher", subjectId,
            true, List.of(), null, null);
    }
}
