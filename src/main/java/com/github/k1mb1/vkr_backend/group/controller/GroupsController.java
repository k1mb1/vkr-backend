package com.github.k1mb1.vkr_backend.group.controller;

import com.github.k1mb1.vkr_backend.group.service.GroupService;
import com.github.k1mb1.vkr_backend.group.service.dto.filter.GroupFilter;
import com.github.k1mb1.vkr_backend.group.service.dto.request.CreateGroupRequest;
import com.github.k1mb1.vkr_backend.group.service.dto.request.UpdateGroupRequest;
import com.github.k1mb1.vkr_backend.group.service.dto.response.GroupPageResponse;
import com.github.k1mb1.vkr_backend.group.service.dto.response.GroupResponse;
import com.github.k1mb1.vkr_backend.group.service.dto.response.GroupWithSubgroupsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping(value = "/api/groups", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Groups", description = "Управление группами и их составом")
@RestController
@RequiredArgsConstructor
public class GroupsController {

    final GroupService groupService;

    @Operation(summary = "Получить страницу групп с фильтрацией по названию")
    @GetMapping
    public ResponseEntity<Page<GroupPageResponse>> getGroupsPage(
            @Valid @ParameterObject @ModelAttribute GroupFilter filter, @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(groupService.getGroupPage(filter, pageable));
    }

    @Operation(summary = "Создать новую группу со списком студентов")
    @PostMapping
    public ResponseEntity<GroupResponse> createGroup(
            @Valid @RequestBody
                    @io.swagger.v3.oas.annotations.parameters.RequestBody(
                            description = "Данные для создания группы",
                            required = true)
                    CreateGroupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(groupService.createGroup(request));
    }

    @Operation(summary = "Частично обновить группу")
    @PatchMapping("/{id}")
    public ResponseEntity<GroupResponse> updateGroup(
            @Parameter(description = "ID группы") @PathVariable UUID id,
            @Valid @RequestBody
                    @io.swagger.v3.oas.annotations.parameters.RequestBody(
                            description = "Данные для обновления группы",
                            required = true)
                    UpdateGroupRequest request) {
        return ResponseEntity.ok(groupService.updateGroup(id, request));
    }

    @Operation(summary = "Получить группу по ID")
    @GetMapping("/{id}")
    public ResponseEntity<GroupResponse> getGroupById(@Parameter(description = "ID группы") @PathVariable UUID id) {
        return ResponseEntity.ok(groupService.getGroupById(id));
    }

    @Operation(summary = "Получить группы, привязанные к предмету (с подгруппами, без студентов)")
    @GetMapping("/by-subject")
    public ResponseEntity<List<GroupWithSubgroupsResponse>> getGroupsBySubject(
            @Parameter(description = "ID предмета") @RequestParam UUID subjectId) {
        return ResponseEntity.ok(groupService.getGroupsBySubjectId(subjectId));
    }

    @Operation(summary = "Удалить группу")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGroup(@Parameter(description = "ID группы") @PathVariable UUID id) {
        groupService.deleteGroup(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Прикрепить группу к предмету")
    @PostMapping("/{groupId}/subjects/{subjectId}")
    public ResponseEntity<GroupResponse> attachToSubject(
            @Parameter(description = "ID группы") @PathVariable UUID groupId,
            @Parameter(description = "ID предмета") @PathVariable UUID subjectId) {
        return ResponseEntity.ok(groupService.attachToSubject(groupId, subjectId));
    }

    @Operation(summary = "Открепить группу от предмета")
    @DeleteMapping("/{groupId}/subjects/{subjectId}")
    public ResponseEntity<Void> detachFromSubject(
            @Parameter(description = "ID группы") @PathVariable UUID groupId,
            @Parameter(description = "ID предмета") @PathVariable UUID subjectId) {
        groupService.detachFromSubject(groupId, subjectId);
        return ResponseEntity.noContent().build();
    }
}
