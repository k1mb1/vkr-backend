package com.github.k1mb1.vkr_backend.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.k1mb1.vkr_backend.attendance.checkin.repository.CheckInSessionRepository;
import com.github.k1mb1.vkr_backend.lesson.repository.LessonRepository;
import com.github.k1mb1.vkr_backend.lesson.repository.LessonScopeRepository;
import com.github.k1mb1.vkr_backend.subject.repository.TeacherSubjectPermissionRepository;
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
class AuthorizationServiceTest {

    @Mock
    SecurityService security;

    @Mock
    PermissionResolver permissionResolver;

    @Mock
    LessonScopeRepository lessonScopeRepository;

    @Mock
    LessonRepository lessonRepository;

    @Mock
    CheckInSessionRepository checkInSessionRepository;

    @Mock
    TeacherSubjectPermissionRepository permissionRepository;

    @InjectMocks
    AuthorizationService authz;

    final UUID userId = UUID.randomUUID();

    private void asUserWith(UserPermissions permissions) {
        when(security.isAdmin()).thenReturn(false);
        lenient().when(security.currentSubjectId()).thenReturn(Optional.of(userId));
        lenient().when(permissionResolver.forUser(userId)).thenReturn(permissions);
    }

    // ---- admin short-circuit ----

    @Test
    void adminAccessesEverythingWithoutResolvingPermissions() {
        when(security.isAdmin()).thenReturn(true);

        assertThat(authz.ownsPermission(UUID.randomUUID())).isTrue();
        assertThat(authz.canAccessSubject(UUID.randomUUID())).isTrue();
        assertThat(authz.canAccessLessons(List.of(UUID.randomUUID()))).isTrue();
        assertThat(authz.canAccessCheckInSession(UUID.randomUUID())).isTrue();

        verify(permissionResolver, never()).forUser(any());
        verify(lessonRepository, never()).findSubjectIdsByLessonIds(any());
    }

    @Test
    void isSelfOrAdminTrueForSameUser() {
        when(security.isAdmin()).thenReturn(false);
        when(security.isSameUser(userId)).thenReturn(true);

        assertThat(authz.isSelfOrAdmin(userId)).isTrue();
    }

    @Test
    void isSelfOrAdminTrueForAdminEvenOnAnotherUser() {
        when(security.isAdmin()).thenReturn(true);

        assertThat(authz.isSelfOrAdmin(UUID.randomUUID())).isTrue();
        verify(security, never()).isSameUser(any());
    }

    @Test
    void isSelfOrAdminFalseWhenNeitherAdminNorSelf() {
        var other = UUID.randomUUID();
        when(security.isAdmin()).thenReturn(false);
        when(security.isSameUser(other)).thenReturn(false);

        assertThat(authz.isSelfOrAdmin(other)).isFalse();
    }

    // ---- canAccessLessons ----

    @Test
    void canAccessLessonsAllowedWhenEverySubjectOwned() {
        var subjectId = UUID.randomUUID();
        asUserWith(new UserPermissions(Set.of(), Set.of(subjectId)));
        var lessonId = UUID.randomUUID();
        when(lessonRepository.findSubjectIdsByLessonIds(List.of(lessonId))).thenReturn(Set.of(subjectId));

        assertThat(authz.canAccessLessons(List.of(lessonId))).isTrue();
    }

    @Test
    void canAccessLessonsDeniedWhenForeignSubjectMixedIn() {
        var ownedSubject = UUID.randomUUID();
        asUserWith(new UserPermissions(Set.of(), Set.of(ownedSubject)));
        var l1 = UUID.randomUUID();
        var l2 = UUID.randomUUID();
        when(lessonRepository.findSubjectIdsByLessonIds(List.of(l1, l2)))
                .thenReturn(Set.of(ownedSubject, UUID.randomUUID()));

        assertThat(authz.canAccessLessons(List.of(l1, l2))).isFalse();
    }

    @Test
    void canAccessLessonsEmptyCollectionAllowed() {
        when(security.isAdmin()).thenReturn(false);

        assertThat(authz.canAccessLessons(List.of())).isTrue();
        verify(lessonRepository, never()).findSubjectIdsByLessonIds(any());
    }

    // ---- ownsPermission / canAccessSubject ----

    @Test
    void ownsPermissionDelegatesToSnapshot() {
        var permId = UUID.randomUUID();
        asUserWith(new UserPermissions(Set.of(permId), Set.of()));

        assertThat(authz.ownsPermission(permId)).isTrue();
        assertThat(authz.ownsPermission(UUID.randomUUID())).isFalse();
    }

    @Test
    void canAccessSubjectDelegatesToSnapshot() {
        var subjectId = UUID.randomUUID();
        asUserWith(new UserPermissions(Set.of(), Set.of(subjectId)));

        assertThat(authz.canAccessSubject(subjectId)).isTrue();
        assertThat(authz.canAccessSubject(UUID.randomUUID())).isFalse();
    }

    // ---- canManageSubject / canManagePermission ----

    @Test
    void canManageSubjectRequiresFullAccessNotJustAnyAccess() {
        var scopedSubject = UUID.randomUUID();
        var fullSubject = UUID.randomUUID();
        asUserWith(new UserPermissions(Set.of(), Set.of(scopedSubject, fullSubject), Set.of(fullSubject)));

        assertThat(authz.canManageSubject(fullSubject)).isTrue();
        // доступ есть (scope), но полного нет — управлять нельзя
        assertThat(authz.canManageSubject(scopedSubject)).isFalse();
        assertThat(authz.canManageSubject(UUID.randomUUID())).isFalse();
    }

    @Test
    void adminCanManageAnySubjectWithoutResolvingPermissions() {
        when(security.isAdmin()).thenReturn(true);

        assertThat(authz.canManageSubject(UUID.randomUUID())).isTrue();
        assertThat(authz.canManagePermission(UUID.randomUUID())).isTrue();

        verify(permissionResolver, never()).forUser(any());
        verify(permissionRepository, never()).findSubjectIdById(any());
    }

    @Test
    void canManagePermissionResolvedViaSubjectFullAccess() {
        var fullSubject = UUID.randomUUID();
        asUserWith(new UserPermissions(Set.of(), Set.of(fullSubject), Set.of(fullSubject)));
        var permissionId = UUID.randomUUID();
        when(permissionRepository.findSubjectIdById(permissionId)).thenReturn(Optional.of(fullSubject));

        assertThat(authz.canManagePermission(permissionId)).isTrue();
    }

    @Test
    void canManagePermissionDeniedWhenOnlyScopedAccessToSubject() {
        var scopedSubject = UUID.randomUUID();
        asUserWith(new UserPermissions(Set.of(), Set.of(scopedSubject), Set.of()));
        var permissionId = UUID.randomUUID();
        when(permissionRepository.findSubjectIdById(permissionId)).thenReturn(Optional.of(scopedSubject));

        assertThat(authz.canManagePermission(permissionId)).isFalse();
    }

    @Test
    void canManagePermissionDeniedWhenPermissionMissingOrNull() {
        when(security.isAdmin()).thenReturn(false);
        var permissionId = UUID.randomUUID();
        lenient().when(permissionRepository.findSubjectIdById(permissionId)).thenReturn(Optional.empty());

        assertThat(authz.canManagePermission(permissionId)).isFalse();
        assertThat(authz.canManagePermission(null)).isFalse();
    }

    @Test
    void deniesWhenNoIdentity() {
        when(security.isAdmin()).thenReturn(false);
        when(security.currentSubjectId()).thenReturn(Optional.empty());

        assertThat(authz.ownsPermission(UUID.randomUUID())).isFalse();
    }

    // ---- canAccessLessonScopes ----

    @Test
    void lessonScopesEmptyCollectionIsAllowed() {
        when(security.isAdmin()).thenReturn(false);

        assertThat(authz.canAccessLessonScopes(List.of())).isTrue();
        verify(lessonScopeRepository, never()).findSubjectIdsByScopeIds(any());
    }

    @Test
    void lessonScopesAllowedWhenAllSubjectsOwned() {
        var subjectId = UUID.randomUUID();
        asUserWith(new UserPermissions(Set.of(), Set.of(subjectId)));
        var scopeId = UUID.randomUUID();
        when(lessonScopeRepository.findSubjectIdsByScopeIds(List.of(scopeId))).thenReturn(Set.of(subjectId));

        assertThat(authz.canAccessLessonScopes(List.of(scopeId))).isTrue();
    }

    @Test
    void lessonScopesDeniedWhenForeignSubjectPresent() {
        var ownedSubject = UUID.randomUUID();
        asUserWith(new UserPermissions(Set.of(), Set.of(ownedSubject)));
        var scopeId = UUID.randomUUID();
        when(lessonScopeRepository.findSubjectIdsByScopeIds(List.of(scopeId)))
                .thenReturn(Set.of(ownedSubject, UUID.randomUUID()));

        assertThat(authz.canAccessLessonScopes(List.of(scopeId))).isFalse();
    }

    // ---- canAccessLessons / canAccessLesson ----

    @Test
    void canAccessLessonDeniedWhenSubjectsUnknown() {
        asUserWith(new UserPermissions(Set.of(), Set.of(UUID.randomUUID())));
        var lessonId = UUID.randomUUID();
        when(lessonRepository.findSubjectIdsByLessonIds(List.of(lessonId))).thenReturn(Set.of());

        // пустой набор предметов => нет подтверждённого доступа
        assertThat(authz.canAccessLesson(lessonId)).isFalse();
    }

    @Test
    void canAccessLessonNullIsTreatedAsEmptyAndAllowed() {
        when(security.isAdmin()).thenReturn(false);

        assertThat(authz.canAccessLesson(null)).isTrue();
    }

    // ---- canAccessCheckInSession ----

    @Test
    void checkInSessionAccessResolvedViaSubject() {
        var subjectId = UUID.randomUUID();
        asUserWith(new UserPermissions(Set.of(), Set.of(subjectId)));
        var sessionId = UUID.randomUUID();
        when(checkInSessionRepository.findSubjectIdById(sessionId)).thenReturn(Optional.of(subjectId));

        assertThat(authz.canAccessCheckInSession(sessionId)).isTrue();
    }

    @Test
    void checkInSessionDeniedWhenSessionMissing() {
        when(security.isAdmin()).thenReturn(false);
        var sessionId = UUID.randomUUID();
        when(checkInSessionRepository.findSubjectIdById(sessionId)).thenReturn(Optional.empty());

        assertThat(authz.canAccessCheckInSession(sessionId)).isFalse();
    }

    @Test
    void checkInSessionDeniedForNullId() {
        when(security.isAdmin()).thenReturn(false);

        assertThat(authz.canAccessCheckInSession(null)).isFalse();
    }
}
