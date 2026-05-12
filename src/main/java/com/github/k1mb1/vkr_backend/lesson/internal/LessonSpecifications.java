package com.github.k1mb1.vkr_backend.lesson.internal;

import com.github.k1mb1.vkr_backend.lesson.domain.Lesson;
import com.github.k1mb1.vkr_backend.lesson.web.filters.LessonFilter;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

record LessonSpecifications(LessonFilter filter) {

    private static Specification<Lesson> bySubjectId(UUID subjectId) {
        if (subjectId == null) {
            return null;
        }
        return (root, query, cb) -> {
            var offering = root.join("offering");
            var subject = offering.join("subject");
            return cb.equal(subject.get("id"), subjectId);
        };
    }

    public Specification<Lesson> toSpec() {
        return bySubjectId(filter.subjectId());
    }
}
