package com.github.k1mb1.vkr_backend.common.security;

import com.github.k1mb1.vkr_backend.attendance.checkin.internal.CheckInSessionRepository;
import com.github.k1mb1.vkr_backend.lesson.internal.LessonRepository;
import com.github.k1mb1.vkr_backend.lesson.internal.LessonScopeRepository;
import com.github.k1mb1.vkr_backend.subject.internal.TeacherSubjectPermissionRepository;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Точка принятия решений авторизации, вызывается из SpEL в {@code @PreAuthorize}
 * как бин {@code @authz}. Identity берётся из токена ({@link SecurityService}),
 * тонкие права — из кэшированного снапшота ({@link PermissionResolver}).
 *
 * <p>Везде {@code isAdmin()} проверяется первым: для админа права из БД не резолвятся
 * вовсе (short-circuit), лишних запросов нет.
 */
@Component("authz")
@RequiredArgsConstructor
public class AuthorizationService {

    private final SecurityService security;
    private final PermissionResolver permissionResolver;
    private final LessonScopeRepository lessonScopeRepository;
    private final LessonRepository lessonRepository;
    private final CheckInSessionRepository checkInSessionRepository;
    private final TeacherSubjectPermissionRepository permissionRepository;

    public boolean isAdmin() {
        return security.isAdmin();
    }

    /** Текущий пользователь действует от своего же имени (teacherId == sub) или это админ. */
    public boolean isSelfOrAdmin(UUID teacherId) {
        return security.isAdmin() || security.isSameUser(teacherId);
    }

    /** Доступ к конкретному выданному permission'у (его таблицам/данным). */
    public boolean ownsPermission(UUID permissionId) {
        return security.isAdmin()
                || current().map(p -> p.ownsPermission(permissionId)).orElse(false);
    }

    /** Доступ к предмету (есть хоть одно право на него). */
    public boolean canAccessSubject(UUID subjectId) {
        return security.isAdmin() || current().map(p -> p.hasSubject(subjectId)).orElse(false);
    }

    /**
     * Право управлять предметом: админ либо преподаватель с полным доступом
     * ({@code allPermissions=true}) на этот предмет. В отличие от {@link #canAccessSubject},
     * преподавателю со scope-ограниченным правом управление недоступно.
     */
    public boolean canManageSubject(UUID subjectId) {
        return security.isAdmin()
                || current().map(p -> p.hasFullAccessToSubject(subjectId)).orElse(false);
    }

    /** Право управлять предметом, к которому относится выданное право (по его id). */
    public boolean canManagePermission(UUID permissionId) {
        return security.isAdmin()
                || permissionId != null
                        && permissionRepository
                                .findSubjectIdById(permissionId)
                                .map(this::canManageSubject)
                                .orElse(false);
    }

    /** Все указанные scope'ы (проведения занятий) принадлежат предметам, доступным пользователю. */
    public boolean canAccessLessonScopes(Collection<UUID> lessonScopeIds) {
        if (security.isAdmin()) {
            return true;
        }
        if (lessonScopeIds == null || lessonScopeIds.isEmpty()) {
            return true;
        }
        var perms = current().orElse(null);
        if (perms == null) {
            return false;
        }
        var subjectIds = lessonScopeRepository.findSubjectIdsByScopeIds(lessonScopeIds);
        return !subjectIds.isEmpty() && perms.subjectIds().containsAll(subjectIds);
    }

    /** Все указанные занятия принадлежат предметам, доступным пользователю. */
    public boolean canAccessLessons(Collection<UUID> lessonIds) {
        if (security.isAdmin()) {
            return true;
        }
        if (lessonIds == null || lessonIds.isEmpty()) {
            return true;
        }
        var perms = current().orElse(null);
        if (perms == null) {
            return false;
        }
        var subjectIds = lessonRepository.findSubjectIdsByLessonIds(lessonIds);
        return !subjectIds.isEmpty() && perms.subjectIds().containsAll(subjectIds);
    }

    public boolean canAccessLesson(UUID lessonId) {
        return canAccessLessons(lessonId == null ? java.util.List.of() : java.util.List.of(lessonId));
    }

    /** Доступ к check-in сессии по её id (через предмет занятия сессии). */
    public boolean canAccessCheckInSession(UUID sessionId) {
        return security.isAdmin()
                || sessionId != null
                        && checkInSessionRepository
                                .findSubjectIdById(sessionId)
                                .map(this::canAccessSubject)
                                .orElse(false);
    }

    private Optional<UserPermissions> current() {
        return security.currentSubjectId().map(permissionResolver::forUser);
    }
}
