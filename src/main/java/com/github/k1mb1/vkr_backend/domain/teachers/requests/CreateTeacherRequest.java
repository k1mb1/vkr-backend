package com.github.k1mb1.vkr_backend.domain.teachers.requests;

import jakarta.validation.constraints.NotBlank;

public record CreateTeacherRequest(
        @NotBlank String name
) {
}
//TODO @Shema like https://github.com/k1mb1/edu/blob/main/src/main/java/me/k1mb/edu/controller/model/CourseRequest.java

