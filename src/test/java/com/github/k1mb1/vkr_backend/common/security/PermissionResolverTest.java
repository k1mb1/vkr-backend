package com.github.k1mb1.vkr_backend.common.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.k1mb1.vkr_backend.subject.internal.TeacherSubjectPermissionRepository;
import com.github.k1mb1.vkr_backend.subject.internal.TeacherSubjectPermissionRepository.OwnedPermissionView;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PermissionResolverTest {

    @Mock
    TeacherSubjectPermissionRepository permissionRepository;

    @InjectMocks
    PermissionResolver resolver;

    private OwnedPermissionView view(UUID permissionId, UUID subjectId) {
        var v = mock(OwnedPermissionView.class);
        when(v.getPermissionId()).thenReturn(permissionId);
        when(v.getSubjectId()).thenReturn(subjectId);
        return v;
    }

    @Test
    void collectsPermissionAndSubjectIds() {
        var teacherId = UUID.randomUUID();
        var p1 = UUID.randomUUID();
        var p2 = UUID.randomUUID();
        var subject = UUID.randomUUID();
        var views = List.of(view(p1, subject), view(p2, subject));
        when(permissionRepository.findOwnedByTeacherId(teacherId)).thenReturn(views);

        var result = resolver.forUser(teacherId);

        assertThat(result.permissionIds()).containsExactlyInAnyOrder(p1, p2);
        assertThat(result.subjectIds()).containsExactly(subject);
    }

    @Test
    void emptyWhenNoPermissions() {
        var teacherId = UUID.randomUUID();
        when(permissionRepository.findOwnedByTeacherId(teacherId)).thenReturn(List.of());

        var result = resolver.forUser(teacherId);

        assertThat(result.permissionIds()).isEmpty();
        assertThat(result.subjectIds()).isEmpty();
    }
}
