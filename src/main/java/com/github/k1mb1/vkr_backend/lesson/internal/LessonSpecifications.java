package com.github.k1mb1.vkr_backend.lesson.internal;

import com.github.k1mb1.vkr_backend.lesson.domain.Lesson;
import com.github.k1mb1.vkr_backend.subject.domain.TeacherSubjectPermission;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public record LessonSpecifications(TeacherSubjectPermission permission) {
    public Specification<Lesson> toSpecification() {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(
                cb.equal(
                    root.get("subject").get("id"),
                    permission.getSubject().getId()
                )
            );
            predicates.add(
                cb.equal(
                    root.get("group").get("id"),
                    permission.getGroup().getId()
                )
            );

            if (permission.getAllowedSubgroup() != null) {
                predicates.add(
                    cb.equal(
                        root.get("subgroup").get("id"),
                        permission.getAllowedSubgroup().getId()
                    )
                );
            }

            if (permission.getAllowedLessonType() != null) {
                predicates.add(
                    cb.equal(
                        root.get("type"),
                        permission.getAllowedLessonType()
                    )
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
