package com.github.k1mb1.vkr_backend.education.structure.api;

import com.github.k1mb1.vkr_backend.education.structure.internal.TeacherEntity;
import lombok.Builder;
import org.springframework.data.jpa.domain.Specification;

@Builder
public record TeacherFilter(String username, String email) {
    public Specification<TeacherEntity> toSpecification() {
        return Specification.where(usernameSpec()).and(emailSpec());
    }

    private Specification<TeacherEntity> usernameSpec() {
        return (root, query, cb) -> username != null && !username.isBlank()
            ? cb.like(cb.lower(root.get("username")), "%" + username.toLowerCase() + "%")
            : null;
    }

    private Specification<TeacherEntity> emailSpec() {
        return (root, query, cb) -> email != null && !email.isBlank()
            ? cb.like(cb.lower(root.get("email")), "%" + email.toLowerCase() + "%")
            : null;
    }
}
