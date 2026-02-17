package com.github.k1mb1.vkr_backend.domain.student_groups;

import com.github.k1mb1.vkr_backend.apis.StudentGroupApi;
import com.github.k1mb1.vkr_backend.domain.student_groups.requests.CreateStudentGroupRequest;
import com.github.k1mb1.vkr_backend.domain.student_groups.requests.UpdateStudentGroupRequest;
import com.github.k1mb1.vkr_backend.domain.student_groups.responses.StudentGroupDetailResponse;
import com.github.k1mb1.vkr_backend.domain.student_groups.responses.StudentGroupResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class StudentGroupController implements StudentGroupApi {

    private final StudentGroupService groupService;

    @Override
    public ResponseEntity<Page<StudentGroupResponse>> findAll(GroupFilter filter, Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(groupService.findAll(filter, pageable));
    }

    @Override
    public ResponseEntity<StudentGroupDetailResponse> findById(UUID id) {
        return ResponseEntity.status(HttpStatus.OK).body(groupService.findById(id));
    }

    @Override
    public ResponseEntity<StudentGroupResponse> create(CreateStudentGroupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(groupService.create(request));
    }

    @Override
    public ResponseEntity<StudentGroupResponse> update(UUID id, UpdateStudentGroupRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(groupService.update(id, request));
    }

    @Override
    public ResponseEntity<Void> delete(UUID id) {
        groupService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}