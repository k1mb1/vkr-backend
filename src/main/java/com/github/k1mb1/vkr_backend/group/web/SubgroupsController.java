package com.github.k1mb1.vkr_backend.group.web;

import com.github.k1mb1.vkr_backend.group.SubgroupsApi;
import com.github.k1mb1.vkr_backend.group.web.response.SubgroupResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping(value = "/api/subgroups", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Subgroups", description = "Управление подгруппами")
@RestController
@RequiredArgsConstructor
public class SubgroupsController {

    final SubgroupsApi subgroupsApi;

    @Operation(summary = "Получить список подгрупп группы")
    @GetMapping
    public ResponseEntity<List<SubgroupResponse>> getSubgroups(
            @Parameter(description = "ID группы") @RequestParam UUID groupId) {
        return ResponseEntity.ok(subgroupsApi.getSubgroups(groupId));
    }
}
