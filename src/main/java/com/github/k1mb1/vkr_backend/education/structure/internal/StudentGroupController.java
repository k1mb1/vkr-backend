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
        return new ResponseEntity<>(studentGroupService.findAll(filter, pageable), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<GroupResponse> create(CreateGroupRequest request) {
        return new ResponseEntity<>(studentGroupService.create(request), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<GroupResponse> findGroupWithSubgroups(UUID groupId) {
        return new ResponseEntity<>(studentGroupService.findGroupWithSubgroups(groupId), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<GroupResponse> update(UUID groupId, UpdateGroupRequest request) {
        return new ResponseEntity<>(studentGroupService.update(groupId, request), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> delete(UUID groupId) {
        studentGroupService.delete(groupId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
