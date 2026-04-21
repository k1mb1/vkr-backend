package com.github.k1mb1.vkr_backend.domain.students;

import java.util.UUID;
import lombok.Builder;
import org.springframework.data.jpa.domain.Specification;

@Builder
public record StudentFilter(
    String username,
    UUID groupId
) {

    public Specification<StudentEntity> toSpecification() {
        return Specification.where(usernameSpec()).and(groupIdSpec());
    }

    private Specification<StudentEntity> usernameSpec() {
        return (root, query, cb) ->
            username != null && !username.isBlank()
                ? cb.like(
                    cb.lower(root.get("username")),
                    "%" + username.toLowerCase() + "%"
                )
                : null;
    }

    private Specification<StudentEntity> groupIdSpec() {
        return (root, query, cb) ->
            groupId != null
                ? cb.equal(root.get("group").get("id"), groupId)
                : null;
    }
}
