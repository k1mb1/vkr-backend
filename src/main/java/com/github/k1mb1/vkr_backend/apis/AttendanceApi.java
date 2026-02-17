package com.github.k1mb1.vkr_backend.apis;

import com.github.k1mb1.vkr_backend.domain.attendances.AttendanceFilter;
import com.github.k1mb1.vkr_backend.domain.attendances.requests.CreateAttendanceRequest;
import com.github.k1mb1.vkr_backend.domain.attendances.requests.UpdateAttendanceRequest;
import com.github.k1mb1.vkr_backend.domain.attendances.responses.AttendanceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequestMapping(
        value = "/api/attendances",
        produces = MediaType.APPLICATION_JSON_VALUE
)
@Tag(name = "Attendances", description = "Attendance management")
public interface AttendanceApi {

    @Operation(summary = "List all attendances")
    @GetMapping
    ResponseEntity<Page<AttendanceResponse>> findAll(
            @ParameterObject @ModelAttribute AttendanceFilter filter,
            @ParameterObject Pageable pageable
    );

    @Operation(summary = "Get attendance by id")
    @GetMapping("/{id}")
    ResponseEntity<AttendanceResponse> findById(@PathVariable UUID id);

    @Operation(summary = "Create attendance")
    @PostMapping
    ResponseEntity<AttendanceResponse> create(@RequestBody @Valid CreateAttendanceRequest request);

    @Operation(summary = "Update attendance")
    @PatchMapping("/{id}")
    ResponseEntity<AttendanceResponse> update(@PathVariable UUID id, @RequestBody @Valid UpdateAttendanceRequest request);

    @Operation(summary = "Delete attendance")
    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@PathVariable UUID id);
}