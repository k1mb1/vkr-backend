package com.github.k1mb1.vkr_backend.lesson.specification;

import com.github.k1mb1.vkr_backend.lesson.domain.LessonEntity;
import com.github.k1mb1.vkr_backend.lesson.domain.LessonScopeEntity;
import com.github.k1mb1.vkr_backend.subject.domain.PermissionScopeEntity;
import com.github.k1mb1.vkr_backend.subject.domain.TeacherSubjectPermissionEntity;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Subquery;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.jpa.domain.Specification;

public final class LessonSpecifications {

    private LessonSpecifications() {}

    /**
     * Lessons that satisfy a single permission scope:
     * - same subject;
     * - if scope.group is set: exists lesson_scope matching the permission scope
     *   (group + optional subgroup) OR a lesson_scope with allGroups=true;
     * - if scope.group is null (means "all groups"): any lesson of the subject;
     * - lesson.type matches scope.allowedLessonType if restricted.
     */
    public static Specification<LessonEntity> forPermissionScope(PermissionScopeEntity scope) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(
                    root.get("subject").get("id"),
                    scope.getPermission().getSubject().getId()));

            if (scope.getGroup() != null) {
                Subquery<UUID> scopeMatch = query.subquery(UUID.class);
                var ls = scopeMatch.from(LessonScopeEntity.class);
                scopeMatch.select(ls.get("id"));
                List<Predicate> scopePredicates = new ArrayList<>();
                scopePredicates.add(cb.equal(ls.get("lesson"), root));
                Predicate groupMatch =
                        cb.equal(ls.get("group").get("id"), scope.getGroup().getId());
                if (scope.getAllowedSubgroup() != null) {
                    groupMatch = cb.and(
                            groupMatch,
                            cb.or(
                                    cb.isNull(ls.get("allowedSubgroup")),
                                    cb.equal(
                                            ls.get("allowedSubgroup").get("id"),
                                            scope.getAllowedSubgroup().getId())));
                }
                scopePredicates.add(cb.or(cb.isTrue(ls.get("allGroups")), groupMatch));
                scopeMatch.where(scopePredicates.toArray(new Predicate[0]));

                predicates.add(cb.exists(scopeMatch));
            }

            var allowedLessonType = scope.getAllowedLessonType();
            if (allowedLessonType != null) {
                predicates.add(cb.equal(root.get("type"), allowedLessonType));
            }

            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                query.distinct(true);
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Lessons visible to a teacher under a given permission. The lesson must belong to the
     * permission's subject AND match at least one permission scope. A permission scope matches
     * a lesson when:
     * - the lesson has a lesson_scope with allGroups=true OR a lesson_scope whose group (and
     *   optional subgroup) intersects the permission scope; if the permission scope has
     *   group=null ("all groups"), the lesson_scope filter is skipped;
     * - AND the lesson's type equals the permission scope's allowedLessonType (or the permission
     *   scope has no type restriction).
     */
    public static Specification<LessonEntity> forPermission(TeacherSubjectPermissionEntity permission) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(
                    root.get("subject").get("id"), permission.getSubject().getId()));

            if (!permission.isAllPermissions()) {
                List<Predicate> orParts = new ArrayList<>();
                for (var ps : permission.getScopes()) {
                    orParts.add(scopePredicate(ps, root, query, cb));
                }
                predicates.add(orParts.isEmpty() ? cb.disjunction() : cb.or(orParts.toArray(new Predicate[0])));
            }

            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                query.distinct(true);
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static Predicate scopePredicate(
            PermissionScopeEntity ps,
            jakarta.persistence.criteria.Root<LessonEntity> root,
            jakarta.persistence.criteria.CriteriaQuery<?> query,
            jakarta.persistence.criteria.CriteriaBuilder cb) {
        List<Predicate> parts = new ArrayList<>();

        if (ps.getGroup() != null) {
            Subquery<UUID> scopeMatch = query.subquery(UUID.class);
            var ls = scopeMatch.from(LessonScopeEntity.class);
            scopeMatch.select(ls.get("id"));
            Predicate groupMatch =
                    cb.equal(ls.get("group").get("id"), ps.getGroup().getId());
            if (ps.getAllowedSubgroup() != null) {
                groupMatch = cb.and(
                        groupMatch,
                        cb.or(
                                cb.isNull(ls.get("allowedSubgroup")),
                                cb.equal(
                                        ls.get("allowedSubgroup").get("id"),
                                        ps.getAllowedSubgroup().getId())));
            }
            scopeMatch.where(cb.equal(ls.get("lesson"), root), cb.or(cb.isTrue(ls.get("allGroups")), groupMatch));
            parts.add(cb.exists(scopeMatch));
        }

        if (ps.getAllowedLessonType() != null) {
            parts.add(cb.equal(root.get("type"), ps.getAllowedLessonType()));
        }

        return parts.isEmpty() ? cb.conjunction() : cb.and(parts.toArray(new Predicate[0]));
    }

    /**
     * Distinct group ids covered by the permission's explicit scopes. Returns an empty set when
     * allPermissions=true or any scope has a null group (meaning "all groups"). In both cases
     * callers must treat the result as "no group filter" — use {@link #permissionAllowsAllGroups}
     * to detect this before calling.
     */
    public static Set<UUID> permissionGroupIds(TeacherSubjectPermissionEntity permission) {
        if (permissionAllowsAllGroups(permission)) {
            return Set.of();
        }
        return permission.getScopes().stream()
                .map(s -> Objects.requireNonNull(s.getGroup()).getId())
                .collect(Collectors.toSet());
    }

    /**
     * True when the permission grants access to all groups of its subject — either via
     * allPermissions=true, or via at least one scope with group=null.
     */
    public static boolean permissionAllowsAllGroups(TeacherSubjectPermissionEntity permission) {
        return permission.isAllPermissions() || permission.getScopes().stream().anyMatch(ps -> ps.getGroup() == null);
    }

    /**
     * LessonEntity scopes (children of {@code lesson}) that are visible to the given permission.
     * Considers only permission scopes whose allowedLessonType matches the lesson's type
     * (or is unrestricted), then applies group/subgroup matching identical to the table
     * audience logic. Always includes a lesson_scope with allGroups=true if any relevant
     * permission scope exists.
     */
    public static List<LessonScopeEntity> visibleScopes(
            LessonEntity lesson, TeacherSubjectPermissionEntity permission) {
        if (permission.isAllPermissions()) {
            return new ArrayList<>(lesson.getScopes());
        }

        var relevant = permission.getScopes().stream()
                .filter(ps -> ps.getAllowedLessonType() == null || ps.getAllowedLessonType() == lesson.getType())
                .toList();
        if (relevant.isEmpty()) {
            return List.of();
        }

        boolean allowAllGroups = relevant.stream().anyMatch(ps -> ps.getGroup() == null);
        Set<UUID> permittedGroupIds = relevant.stream()
                .filter(ps -> ps.getGroup() != null)
                .map(ps -> ps.getGroup().getId())
                .collect(Collectors.toSet());
        Map<UUID, UUID> subgroupRestrictions = new HashMap<>();
        for (var ps : relevant) {
            if (ps.getGroup() == null) {
                continue;
            }
            if (ps.getAllowedSubgroup() != null) {
                subgroupRestrictions.putIfAbsent(
                        ps.getGroup().getId(), ps.getAllowedSubgroup().getId());
            } else {
                subgroupRestrictions.put(ps.getGroup().getId(), null);
            }
        }

        var result = new ArrayList<LessonScopeEntity>();
        for (var scope : lesson.getScopes()) {
            if (scope.isAllGroups()) {
                result.add(scope);
                continue;
            }
            if (scope.getGroup() == null) {
                continue;
            }
            if (allowAllGroups || permittedGroupIds.contains(scope.getGroup().getId())) {
                var allowedSubgroupId =
                        subgroupRestrictions.get(scope.getGroup().getId());
                if (allowedSubgroupId == null
                        || scope.getAllowedSubgroup() == null
                        || allowedSubgroupId.equals(scope.getAllowedSubgroup().getId())) {
                    result.add(scope);
                }
            }
        }
        return result;
    }
}
