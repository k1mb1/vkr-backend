package com.github.k1mb1.vkr_backend.domain.teachers.requests;

import com.github.k1mb1.vkr_backend.domain.teachers.TeacherEntity;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO for {@link TeacherEntity}
 */
public record CreateTeacherRequest(
        @NotBlank String username,
        @NotBlank String email
) {
}
//TODO @Shema like https://github.com/k1mb1/edu/blob/main/src/main/java/me/k1mb/edu/controller/model/CourseRequest.java

