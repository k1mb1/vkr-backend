package com.github.k1mb1.vkr_backend.lesson.internal;

import com.github.k1mb1.vkr_backend.lesson.domain.Lesson;
import com.github.k1mb1.vkr_backend.lesson.web.filters.LessonFilter;
import org.springframework.data.jpa.domain.Specification;

record LessonSpecifications(LessonFilter filter) {
    public Specification<Lesson> toSpecification() {
        return bySubjectIdSpec();
    }

    private Specification<Lesson> bySubjectIdSpec() {
        return (root, query, cb) -> {
            if (filter.subjectId() == null) {
                return null;
            }
            return cb.equal(root.get("subject").get("id"), filter.subjectId());
        };
    }
}
