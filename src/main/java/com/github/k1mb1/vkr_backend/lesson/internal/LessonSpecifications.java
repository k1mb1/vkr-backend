package com.github.k1mb1.vkr_backend.lesson.internal;

import com.github.k1mb1.vkr_backend.lesson.domain.Lesson;
import com.github.k1mb1.vkr_backend.lesson.domain.LessonScope;
import com.github.k1mb1.vkr_backend.subject.domain.PermissionScope;
import com.github.k1mb1.vkr_backend.subject.domain.TeacherSubjectPermission;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class LessonSpecifications {

    private LessonSpecifications() {
    }

    /**
     * Lessons that satisfy a single permission scope:
     * - same subject;
     * - exists lesson_scope matching the permission scope (group + optional subgroup) OR
     * a lesson_scope with allGroups=true;
     * - lesson.type matches scope.allowedLessonType if restricted.
     */
    public static Specification<Lesson> forPermissionScope(PermissionScope scope) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(
                root.get("subject").get("id"),
                scope.getPermission().getSubject().getId()
            ));

            Subquery<UUID> scopeMatch = query.subquery(UUID.class);
            var ls = scopeMatch.from(LessonScope.class);
            scopeMatch.select(ls.get("id"));
            List<Predicate> scopePredicates = new ArrayList<>();
            scopePredicates.add(cb.equal(ls.get("lesson"), root));
            Predicate groupMatch = cb.equal(ls.get("group").get("id"), scope.getGroup().getId());
            if (scope.getAllowedSubgroup() != null) {
                groupMatch = cb.and(
                    groupMatch, cb.or(
                        cb.isNull(ls.get("allowedSubgroup")),
                        cb.equal(
                            ls.get("allowedSubgroup").get("id"),
                            scope.getAllowedSubgroup().getId()
                        )
                    )
                );
            }
            scopePredicates.add(cb.or(cb.isTrue(ls.get("allGroups")), groupMatch));
            scopeMatch.where(scopePredicates.toArray(new Predicate[0]));

            predicates.add(cb.exists(scopeMatch));

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
     * Lessons visible to a teacher under a given permission:
     * - same subject;
     * - exists lesson_scope with allGroups=true OR intersecting any of the permission's scopes
     * by group (and matching subgroup when restricted).
     */
    public static Specification<Lesson> forPermission(
        TeacherSubjectPermission permission
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(
                root.get("subject").get("id"),
                permission.getSubject().getId()
            ));

            if (!permission.isAllPermissions()) {
                Subquery<UUID> scopeMatch = query.subquery(UUID.class);
                var ls = scopeMatch.from(LessonScope.class);
                scopeMatch.select(ls.get("id"));

                List<Predicate> orParts = new ArrayList<>();
                orParts.add(cb.isTrue(ls.get("allGroups")));
                for (var ps : permission.getScopes()) {
                    List<Predicate> parts = new ArrayList<>();
                    parts.add(cb.equal(ls.get("group").get("id"), ps.getGroup().getId()));
                    if (ps.getAllowedSubgroup() != null) {
                        parts.add(cb.or(
                            cb.isNull(ls.get("allowedSubgroup")),
                            cb.equal(
                                ls.get("allowedSubgroup").get("id"),
                                ps.getAllowedSubgroup().getId()
                            )
                        ));
                    }
                    orParts.add(cb.and(parts.toArray(new Predicate[0])));
                }
                Predicate scopeIntersect = cb.or(orParts.toArray(new Predicate[0]));
                scopeMatch.where(cb.equal(ls.get("lesson"), root), scopeIntersect);

                predicates.add(cb.exists(scopeMatch));
            }

            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                query.distinct(true);
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Distinct group ids covered by the permission's explicit scopes. Returns an empty set when
     * allPermissions=true — callers must short-circuit on allPermissions before using this and
     * treat allPermissions as "no group filter".
     */
    public static Set<UUID> permissionGroupIds(TeacherSubjectPermission permission) {
        if (permission.isAllPermissions()) {
            return Set.of();
        }
        return permission.getScopes()
            .stream()
            .map(s -> s.getGroup().getId())
            .collect(Collectors.toSet());
    }
}
