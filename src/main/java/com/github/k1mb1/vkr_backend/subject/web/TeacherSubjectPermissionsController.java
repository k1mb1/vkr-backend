package com.github.k1mb1.vkr_backend.subject.web;

import com.github.k1mb1.vkr_backend.subject.TeacherSubjectPermissionsApi;
import com.github.k1mb1.vkr_backend.subject.web.requests.CreateTeacherSubjectPermissionRequest;
import com.github.k1mb1.vkr_backend.subject.web.requests.UpdateTeacherSubjectPermissionRequest;
import com.github.k1mb1.vkr_backend.subject.web.responses.TeacherSubjectPermissionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(
    value = "/api/teacher-subject-permissions", produces = MediaType.APPLICATION_JSON_VALUE
)
@Tag(
    name = "Teacher Subject Permissions", description = "Права преподавателей на предметы"
)
@RequiredArgsConstructor
public class TeacherSubjectPermissionsController {

    final TeacherSubjectPermissionsApi teacherSubjectPermissionsApi;

    @Operation(summary = "Получить список прав преподавателей на предмет")
    @GetMapping
    public ResponseEntity<List<TeacherSubjectPermissionResponse>> getTeacherSubjectPermissions(
        @Parameter(description = "ID предмета")
        @RequestParam UUID subjectId
    ) {
        return ResponseEntity.ok(teacherSubjectPermissionsApi.getPermissionsBySubject(subjectId));
    }

    @Operation(summary = "Получить право преподавателя на предмет")
    @GetMapping("/single")
    public ResponseEntity<TeacherSubjectPermissionResponse> getTeacherSubjectPermission(
        @Parameter(description = "ID предмета")
        @RequestParam UUID subjectId,
        @Parameter(description = "ID преподавателя")
        @RequestParam UUID teacherId
    ) {
        return ResponseEntity.ok(teacherSubjectPermissionsApi.getPermission(subjectId, teacherId));
    }

    @Operation(summary = "Создать право преподавателя на предмет")
    @PostMapping
    public ResponseEntity<TeacherSubjectPermissionResponse> createTeacherSubjectPermission(
        @Valid
        @RequestBody
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Данные для создания права", required = true
        )
        CreateTeacherSubjectPermissionRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(teacherSubjectPermissionsApi.create(request));
    }

    @Operation(summary = "Обновить право преподавателя на предмет")
    @PatchMapping("/{id}")
    public ResponseEntity<TeacherSubjectPermissionResponse> updateTeacherSubjectPermission(
        @Parameter(description = "ID права")
        @PathVariable UUID id,
        @Valid
        @RequestBody
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Данные для обновления права", required = true
        )
        UpdateTeacherSubjectPermissionRequest request
    ) {
        return ResponseEntity.ok(teacherSubjectPermissionsApi.update(id, request));
    }

    @Operation(
        summary = "Удалить (архивировать) право преподавателя на предмет"
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTeacherSubjectPermission(
        @Parameter(description = "ID права")
        @PathVariable UUID id
    ) {
        teacherSubjectPermissionsApi.delete(id);
        return ResponseEntity.noContent().build();
    }
}
