package com.github.k1mb1.vkr_backend.lesson.internal;

import com.github.k1mb1.vkr_backend.lesson.domain.Lesson;
import com.github.k1mb1.vkr_backend.lesson.web.filters.LessonFilter;
import com.github.k1mb1.vkr_backend.subject.domain.TeacherSubjectPermission;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

record LessonSpecifications(LessonFilter filter) {
    public Specification<Lesson> toSpecification() {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("subject").get("id"), filter.subjectId()));

            Subquery<Long> sq = query.subquery(Long.class);
            Root<TeacherSubjectPermission> perm = sq.from(TeacherSubjectPermission.class);
            sq.select(cb.literal(1L));

            List<Predicate> subPredicates = new ArrayList<>();
            subPredicates.add(cb.equal(perm.get("teacher").get("id"), filter.teacherId()));
            subPredicates.add(cb.equal(
                perm.get("subject").get("id"),
                root.get("subject").get("id")
            ));
            subPredicates.add(cb.equal(perm.get("group").get("id"), root.get("group").get("id")));
            subPredicates.add(cb.isNull(perm.get("archivedAt")));

            Predicate subgroupPred = cb.or(
                cb.isNull(perm.get("allowedSubgroup")),
                cb.equal(
                    perm.get("allowedSubgroup"),
                    root.get("subgroup")
                )
            );
            subPredicates.add(subgroupPred);

            Predicate typePred = cb.or(
                cb.isNull(perm.get("allowedLessonType")),
                cb.equal(perm.get("allowedLessonType"), root.get("type"))
            );
            subPredicates.add(typePred);

            sq.where(subPredicates.toArray(new Predicate[0]));
            predicates.add(cb.exists(sq));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
