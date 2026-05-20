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
     * - lesson.allGroups OR exists lesson_scope matching scope.group
     * (and matching subgroup if scope restricts it),
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
            scopePredicates.add(cb.equal(ls.get("group").get("id"), scope.getGroup().getId()));
            if (scope.getAllowedSubgroup() != null) {
                scopePredicates.add(cb.or(
                    cb.isNull(ls.get("allowedSubgroup")),
                    cb.equal(
                        ls.get("allowedSubgroup").get("id"),
                        scope.getAllowedSubgroup().getId()
                    )
                ));
            }
            scopeMatch.where(scopePredicates.toArray(new Predicate[0]));

            predicates.add(cb.or(cb.isTrue(root.get("allGroups")), cb.exists(scopeMatch)));

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
     * - lesson.allGroups OR exists lesson_scope intersecting any of the permission's scopes
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
                Predicate scopeIntersect = orParts.isEmpty()
                                           ? cb.disjunction()
                                           : cb.or(orParts.toArray(new Predicate[0]));
                scopeMatch.where(cb.equal(ls.get("lesson"), root), scopeIntersect);

                predicates.add(cb.or(cb.isTrue(root.get("allGroups")), cb.exists(scopeMatch)));
            }

            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                query.distinct(true);
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Convenience: distinct group ids covered by the permission (when allPermissions=false, from
     * scopes; when allPermissions=true, from the subject's attached groups).
     */
    public static Set<UUID> permissionGroupIds(TeacherSubjectPermission permission) {
        if (permission.isAllPermissions()) {
            return permission.getSubject()
                .getGroups()
                .stream()
                .map(g -> g.getId())
                .collect(Collectors.toSet());
        }
        return permission.getScopes()
            .stream()
            .map(s -> s.getGroup().getId())
            .collect(Collectors.toSet());
    }
}
