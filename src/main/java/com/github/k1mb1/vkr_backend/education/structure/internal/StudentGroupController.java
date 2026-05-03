package com.github.k1mb1.vkr_backend.education.structure.internal;

import com.github.k1mb1.vkr_backend.education.structure.api.StudentGroupApi;
import com.github.k1mb1.vkr_backend.education.structure.api.StudentGroupFilter;
import com.github.k1mb1.vkr_backend.education.structure.api.requests.CreateGroupRequest;
import com.github.k1mb1.vkr_backend.education.structure.api.requests.UpdateGroupRequest;
import com.github.k1mb1.vkr_backend.education.structure.api.responses.GroupPageResponse;
import com.github.k1mb1.vkr_backend.education.structure.api.responses.GroupResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Validated
class StudentGroupController implements StudentGroupApi {
    final StudentGroupService studentGroupService;

    @Override
    public ResponseEntity<Page<GroupPageResponse>> findAll(StudentGroupFilter filter, Pageable pageable) {
        return ResponseEntity.ok(studentGroupService.findAll(filter, pageable));
    }

    @Override
    public ResponseEntity<GroupResponse> create(CreateGroupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(studentGroupService.create(request));
    }

    @Override
    public ResponseEntity<GroupResponse> findGroupWithSubgroups(UUID groupId) {
        return ResponseEntity.ok(studentGroupService.findGroupWithSubgroups(groupId));
    }

    @Override
    public ResponseEntity<GroupResponse> update(UUID groupId, UpdateGroupRequest request) {
        return ResponseEntity.ok(studentGroupService.update(groupId, request));
    }

    @Override
    public ResponseEntity<Void> delete(UUID groupId) {
        studentGroupService.delete(groupId);
        return ResponseEntity.noContent().build();
    }
}
