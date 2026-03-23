package com.github.k1mb1.vkr_backend.domain.teachers.requests;

import com.github.k1mb1.vkr_backend.domain.teachers.TeacherEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * DTO for {@link TeacherEntity}
 */
public record CreateTeacherRequest(
    @Schema(
        description = "External teacher id (Keycloak user id). Must be a UUID provided by the client.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull
    UUID id,
    @NotBlank String username,
    @NotBlank String email
) {}
//TODO @Shema like https://github.com/k1mb1/edu/blob/main/src/main/java/me/k1mb/edu/controller/model/CourseRequest.java
