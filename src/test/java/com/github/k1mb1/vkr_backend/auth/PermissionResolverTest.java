package com.github.k1mb1.vkr_backend.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.github.k1mb1.vkr_backend.auth.api.PermissionAuthPort;
import com.github.k1mb1.vkr_backend.auth.api.PermissionGrantResponse;
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
    PermissionAuthPort permissionAuthPort;

    @InjectMocks
    PermissionResolver resolver;

    private PermissionGrantResponse view(UUID permissionId, UUID subjectId) {
        return view(permissionId, subjectId, false);
    }

    private PermissionGrantResponse view(UUID permissionId, UUID subjectId, boolean allPermissions) {
        var v = new PermissionGrantResponse(permissionId, subjectId, allPermissions);
        return v;
    }

    @Test
    void collectsPermissionAndSubjectIds() {
        var teacherId = UUID.randomUUID();
        var p1 = UUID.randomUUID();
        var p2 = UUID.randomUUID();
        var subject = UUID.randomUUID();
        var views = List.of(view(p1, subject), view(p2, subject));
        when(permissionAuthPort.grantsOfTeacher(teacherId)).thenReturn(views);

        var result = resolver.forUser(teacherId);

        assertThat(result.permissionIds()).containsExactlyInAnyOrder(p1, p2);
        assertThat(result.subjectIds()).containsExactly(subject);
    }

    @Test
    void collectsOnlyFullAccessSubjectsIntoManageableSet() {
        var teacherId = UUID.randomUUID();
        var fullSubject = UUID.randomUUID();
        var scopedSubject = UUID.randomUUID();
        var views = List.of(view(UUID.randomUUID(), fullSubject, true), view(UUID.randomUUID(), scopedSubject, false));
        when(permissionAuthPort.grantsOfTeacher(teacherId)).thenReturn(views);

        var result = resolver.forUser(teacherId);

        assertThat(result.subjectIds()).containsExactlyInAnyOrder(fullSubject, scopedSubject);
        assertThat(result.fullAccessSubjectIds()).containsExactly(fullSubject);
        assertThat(result.hasFullAccessToSubject(fullSubject)).isTrue();
        assertThat(result.hasFullAccessToSubject(scopedSubject)).isFalse();
    }

    @Test
    void emptyWhenNoPermissions() {
        var teacherId = UUID.randomUUID();
        when(permissionAuthPort.grantsOfTeacher(teacherId)).thenReturn(List.of());

        var result = resolver.forUser(teacherId);

        assertThat(result.permissionIds()).isEmpty();
        assertThat(result.subjectIds()).isEmpty();
    }
}
