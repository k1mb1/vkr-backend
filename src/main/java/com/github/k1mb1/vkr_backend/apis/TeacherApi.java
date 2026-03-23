package com.github.k1mb1.vkr_backend.apis;

import com.github.k1mb1.vkr_backend.domain.teachers.requests.UpdateTeacherRequest;
import com.github.k1mb1.vkr_backend.domain.teachers.responses.TeacherResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RequestMapping(
    value = "/api/teachers",
    produces = MediaType.APPLICATION_JSON_VALUE
)
@Tag(name = "Teachers", description = "Teacher management")
public interface TeacherApi {
    @Operation(
        summary = "Find or create teacher from JWT token (called on login)"
    )
    @PutMapping("/{id}")
    @PreAuthorize("@securityService.isSameUser(authentication, #id)")
    ResponseEntity<TeacherResponse> createOrUpdate(
        @PathVariable UUID id,
        @RequestBody @Valid UpdateTeacherRequest request
    );
}
