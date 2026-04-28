package com.github.k1mb1.vkr_backend.domain.student_groups;

import com.github.k1mb1.vkr_backend.apis.StudentGroupApi;
import com.github.k1mb1.vkr_backend.domain.student_groups.requests.CreateGroupRequest;
import com.github.k1mb1.vkr_backend.domain.student_groups.requests.UpdateGroupRequest;
import com.github.k1mb1.vkr_backend.domain.student_groups.responses.StudentGroupPageResponse;
import com.github.k1mb1.vkr_backend.domain.student_groups.responses.StudentGroupResponse;
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
public class StudentGroupController implements StudentGroupApi {

    final StudentGroupService studentGroupService;

    @Override
    public ResponseEntity<Page<StudentGroupPageResponse>> findAll(
        StudentGroupFilter filter,
        Pageable pageable
    ) {
        return ResponseEntity.ok(studentGroupService.findAll(filter, pageable));
    }

    @Override
    public ResponseEntity<StudentGroupResponse> create(CreateGroupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(studentGroupService.create(request));
    }

    @Override
    public ResponseEntity<StudentGroupResponse> findGroupWithSubgroups(UUID groupId) {
        return ResponseEntity.ok(studentGroupService.findGroupWithSubgroups(groupId));
    }

    @Override
    public ResponseEntity<StudentGroupResponse> update(UUID groupId, UpdateGroupRequest request) {
        return ResponseEntity.ok(studentGroupService.update(groupId, request));
    }

    @Override
    public ResponseEntity<Void> delete(UUID groupId) {
        studentGroupService.delete(groupId);
        return ResponseEntity.noContent().build();
    }
}
