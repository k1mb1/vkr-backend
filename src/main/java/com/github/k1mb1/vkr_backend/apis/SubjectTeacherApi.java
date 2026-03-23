package com.github.k1mb1.vkr_backend.apis;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RequestMapping(
    value = "/api/subjects/{subjectId}/teachers",
    produces = MediaType.APPLICATION_JSON_VALUE
)
@Tag(
    name = "Subject Teachers",
    description = "Manage teachers assigned to subjects"
)
public interface SubjectTeacherApi {}
