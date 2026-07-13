package com.github.k1mb1.vkr_backend.subject.controller;

import com.github.k1mb1.vkr_backend.subject.service.TeacherSubjectPermissionService;
import com.github.k1mb1.vkr_backend.subject.service.dto.request.CreateTeacherSubjectPermissionRequest;
import com.github.k1mb1.vkr_backend.subject.service.dto.request.UpdateTeacherSubjectPermissionRequest;
import com.github.k1mb1.vkr_backend.subject.service.dto.response.TeacherSubjectPermissionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/teacher-subject-permissions", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Teacher Subject Permissions", description = "Права преподавателей на предметы")
@RequiredArgsConstructor
public class TeacherSubjectPermissionsController {

    final TeacherSubjectPermissionService teacherSubjectPermissionService;

    @Operation(summary = "Получить список прав преподавателей на предмет")
    @GetMapping
    public ResponseEntity<List<TeacherSubjectPermissionResponse>> getTeacherSubjectPermissions(
            @Parameter(description = "ID предмета") @RequestParam UUID subjectId) {
        return ResponseEntity.ok(teacherSubjectPermissionService.getPermissionsBySubject(subjectId));
    }

    @Operation(summary = "Получить право преподавателя на предмет")
    @GetMapping("/single")
    public ResponseEntity<TeacherSubjectPermissionResponse> getTeacherSubjectPermission(
            @Parameter(description = "ID предмета") @RequestParam UUID subjectId,
            @Parameter(description = "ID преподавателя") @RequestParam UUID teacherId) {
        return ResponseEntity.ok(teacherSubjectPermissionService.getPermission(subjectId, teacherId));
    }

    @Operation(summary = "Создать право преподавателя на предмет")
    @PostMapping
    public ResponseEntity<TeacherSubjectPermissionResponse> createTeacherSubjectPermission(
            @Valid @RequestBody
                    @io.swagger.v3.oas.annotations.parameters.RequestBody(
                            description = "Данные для создания права",
                            required = true)
                    CreateTeacherSubjectPermissionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(teacherSubjectPermissionService.create(request));
    }

    @Operation(summary = "Обновить право преподавателя на предмет")
    @PatchMapping("/{id}")
    public ResponseEntity<TeacherSubjectPermissionResponse> updateTeacherSubjectPermission(
            @Parameter(description = "ID права") @PathVariable UUID id,
            @Valid @RequestBody
                    @io.swagger.v3.oas.annotations.parameters.RequestBody(
                            description = "Данные для обновления права",
                            required = true)
                    UpdateTeacherSubjectPermissionRequest request) {
        return ResponseEntity.ok(teacherSubjectPermissionService.update(id, request));
    }

    @Operation(summary = "Удалить (архивировать) право преподавателя на предмет")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTeacherSubjectPermission(
            @Parameter(description = "ID права") @PathVariable UUID id) {
        teacherSubjectPermissionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
