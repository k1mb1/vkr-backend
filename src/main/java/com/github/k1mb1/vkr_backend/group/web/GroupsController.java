package com.github.k1mb1.vkr_backend.group.web;

import com.github.k1mb1.vkr_backend.common.error.ErrorDto;
import com.github.k1mb1.vkr_backend.group.GroupsApi;
import com.github.k1mb1.vkr_backend.group.web.filters.GroupFilter;
import com.github.k1mb1.vkr_backend.group.web.requests.CreateGroupRequest;
import com.github.k1mb1.vkr_backend.group.web.requests.UpdateGroupRequest;
import com.github.k1mb1.vkr_backend.group.web.response.GroupPageResponse;
import com.github.k1mb1.vkr_backend.group.web.response.GroupResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping(
    value = "/api/groups",
    produces = MediaType.APPLICATION_JSON_VALUE
)
@Tag(name = "Groups", description = "Управление группами и их составом")
@RestController
@RequiredArgsConstructor
public class GroupsController {

    final GroupsApi groupsApi;

    @Operation(summary = "Получить страницу групп с фильтрацией по названию")
    @ApiResponses(
        {
            @ApiResponse(responseCode = "200", description = "Успешно"),
            @ApiResponse(
                responseCode = "500",
                description = "Внутренняя ошибка сервера",
                content = @Content(
                    schema = @Schema(implementation = ErrorDto.class)
                )
            ),
        }
    )
    @GetMapping
    public ResponseEntity<Page<GroupPageResponse>> getPage(
        @ParameterObject @ModelAttribute GroupFilter filter,
        @ParameterObject Pageable pageable
    ) {
        return ResponseEntity.ok(groupsApi.getPage(filter, pageable));
    }

    @Operation(summary = "Создать новую группу со списком студентов")
    @ApiResponses(
        {
            @ApiResponse(responseCode = "201", description = "Группа создана"),
            @ApiResponse(
                responseCode = "400",
                description = "Ошибка валидации",
                content = @Content(
                    schema = @Schema(implementation = ErrorDto.class)
                )
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Внутренняя ошибка сервера",
                content = @Content(
                    schema = @Schema(implementation = ErrorDto.class)
                )
            ),
        }
    )
    @PostMapping
    public ResponseEntity<GroupResponse> create(
        @Valid @RequestBody @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Данные для создания группы",
            required = true
        ) CreateGroupRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
            groupsApi.create(request)
        );
    }

    @Operation(summary = "Частично обновить группу")
    @ApiResponses(
        {
            @ApiResponse(
                responseCode = "200",
                description = "Группа обновлена"
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Ошибка валидации",
                content = @Content(
                    schema = @Schema(implementation = ErrorDto.class)
                )
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Группа не найдена",
                content = @Content(
                    schema = @Schema(implementation = ErrorDto.class)
                )
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Внутренняя ошибка сервера",
                content = @Content(
                    schema = @Schema(implementation = ErrorDto.class)
                )
            ),
        }
    )
    @PatchMapping("/{id}")
    public ResponseEntity<GroupResponse> update(
        @Parameter(
            description = "ID группы",
            example = "550e8400-e29b-41d4-a716-446655440000"
        ) @PathVariable UUID id,
        @Valid @RequestBody @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Данные для обновления группы",
            required = true
        ) UpdateGroupRequest request
    ) {
        return ResponseEntity.ok(groupsApi.update(id, request));
    }

    @Operation(summary = "Получить группу по ID")
    @ApiResponses(
        {
            @ApiResponse(responseCode = "200", description = "Успешно"),
            @ApiResponse(
                responseCode = "404",
                description = "Группа не найдена",
                content = @Content(
                    schema = @Schema(implementation = ErrorDto.class)
                )
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Внутренняя ошибка сервера",
                content = @Content(
                    schema = @Schema(implementation = ErrorDto.class)
                )
            ),
        }
    )
    @GetMapping("/{id}")
    public ResponseEntity<GroupResponse> getById(
        @Parameter(
            description = "ID группы",
            example = "550e8400-e29b-41d4-a716-446655440000"
        ) @PathVariable UUID id
    ) {
        return ResponseEntity.ok(groupsApi.getById(id));
    }

    @Operation(summary = "Удалить группу")
    @ApiResponses(
        {
            @ApiResponse(responseCode = "204", description = "Группа удалена"),
            @ApiResponse(
                responseCode = "404",
                description = "Группа не найдена",
                content = @Content(
                    schema = @Schema(implementation = ErrorDto.class)
                )
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Внутренняя ошибка сервера",
                content = @Content(
                    schema = @Schema(implementation = ErrorDto.class)
                )
            ),
        }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
        @Parameter(
            description = "ID группы",
            example = "550e8400-e29b-41d4-a716-446655440000"
        ) @PathVariable UUID id
    ) {
        groupsApi.delete(id);
        return ResponseEntity.noContent().build();
    }
}
