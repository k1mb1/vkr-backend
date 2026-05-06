package com.github.k1mb1.vkr_backend.teachers.web;

import com.github.k1mb1.vkr_backend.teachers.TeachersApi;
import com.github.k1mb1.vkr_backend.teachers.web.requests.CreateOrUpdateTeacherRequest;
import com.github.k1mb1.vkr_backend.teachers.web.responses.TeacherResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RequestMapping(
        value = "/api/teachers",
        produces = MediaType.APPLICATION_JSON_VALUE
)
@Tag(name = "Teachers", description = "Teacher management")
@RestController
@RequiredArgsConstructor
public class TeachersController {

    final TeachersApi teacherService;

    @Operation(summary = "Create or update teacher")
    @PutMapping("/{id}")
    public ResponseEntity<TeacherResponse> createOrUpdate(
            @PathVariable UUID id,
            @RequestBody CreateOrUpdateTeacherRequest request
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(
                teacherService.createOrUpdate(id, request)
        );
    }
}
