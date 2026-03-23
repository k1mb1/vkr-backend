package com.github.k1mb1.vkr_backend.apis;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RequestMapping(
    value = "/api/students",
    produces = MediaType.APPLICATION_JSON_VALUE
)
@Tag(name = "Students", description = "Student management")
public interface StudentApi {}
