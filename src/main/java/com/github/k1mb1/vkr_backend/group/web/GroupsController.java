package com.github.k1mb1.vkr_backend.group.web;

import com.github.k1mb1.vkr_backend.group.GroupsApi;
import com.github.k1mb1.vkr_backend.group.web.filters.GroupFilter;
import com.github.k1mb1.vkr_backend.group.web.requests.CreateGroupRequest;
import com.github.k1mb1.vkr_backend.group.web.requests.UpdateGroupRequest;
import com.github.k1mb1.vkr_backend.group.web.response.GroupPageResponse;
import com.github.k1mb1.vkr_backend.group.web.response.GroupResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequestMapping(
    value = "/api/groups", produces = MediaType.APPLICATION_JSON_VALUE
)
@Tag(name = "Groups", description = "Управление группами и их составом")
@RestController
@RequiredArgsConstructor
public class GroupsController {

    final GroupsApi groupsApi;

    @Operation(summary = "Получить страницу групп с фильтрацией по названию")
    @GetMapping
    public ResponseEntity<Page<GroupPageResponse>> getGroupPage(
        @ParameterObject
        @ModelAttribute
        GroupFilter filter,

        @ParameterObject Pageable pageable
    ) {
        return ResponseEntity.ok(groupsApi.getGroupPage(filter, pageable));
    }

    @Operation(summary = "Создать новую группу со списком студентов")
    @PostMapping
    public ResponseEntity<GroupResponse> createGroup(
        @Valid
        @RequestBody
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Данные для создания группы", required = true
        )
        CreateGroupRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(groupsApi.createGroup(request));
    }

    @Operation(summary = "Частично обновить группу")
    @PatchMapping("/{id}")
    public ResponseEntity<GroupResponse> updateGroup(
        @Parameter(description = "ID группы")
        @PathVariable
        UUID id,

        @Valid
        @RequestBody
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Данные для обновления группы", required = true
        )
        UpdateGroupRequest request
    ) {
        return ResponseEntity.ok(groupsApi.updateGroup(id, request));
    }

    @Operation(summary = "Получить группу по ID")
    @GetMapping("/{id}")
    public ResponseEntity<GroupResponse> getGroupById(
        @Parameter(description = "ID группы")
        @PathVariable
        UUID id
    ) {
        return ResponseEntity.ok(groupsApi.getGroupById(id));
    }

    @Operation(summary = "Удалить группу")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGroup(
        @Parameter(description = "ID группы")
        @PathVariable
        UUID id
    ) {
        groupsApi.deleteGroup(id);
        return ResponseEntity.noContent().build();
    }
}
