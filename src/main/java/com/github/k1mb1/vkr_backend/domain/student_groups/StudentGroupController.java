package com.github.k1mb1.vkr_backend.domain.student_groups;

import com.github.k1mb1.vkr_backend.apis.StudentGroupApi;
import com.github.k1mb1.vkr_backend.domain.student_groups.filters.FindGroupsFilter;
import com.github.k1mb1.vkr_backend.domain.student_groups.requests.CreateGroupRequest;
import com.github.k1mb1.vkr_backend.domain.student_groups.requests.UpdateGroupRequest;
import com.github.k1mb1.vkr_backend.domain.student_groups.responses.StudentGroupPageResponse;
import com.github.k1mb1.vkr_backend.domain.student_groups.responses.StudentGroupResponse;
import com.github.k1mb1.vkr_backend.domain.student_groups.responses.SubgroupResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class StudentGroupController implements StudentGroupApi {

    final StudentGroupService studentGroupService;

    @Override
    public ResponseEntity<Page<StudentGroupPageResponse>> findAll(
        FindGroupsFilter filter,
        Pageable pageable
    ) {
        var groupFilter = StudentGroupFilter.builder().name(filter.name()).build();

        return ResponseEntity.status(HttpStatus.OK).body(
            studentGroupService.findAll(groupFilter, pageable)
        );
    }

    @Override
    public ResponseEntity<StudentGroupResponse> create(
        CreateGroupRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
            studentGroupService.create(request)
        );
    }

    @Override
    public ResponseEntity<StudentGroupResponse> findGroupWithSubgroups(
        UUID groupId
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(
            studentGroupService.findGroupWithSubgroups(groupId)
        );
    }

    @Override
    public ResponseEntity<Page<SubgroupResponse>> findSubgroups(
        UUID groupId,
        Pageable pageable
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(
            studentGroupService.findSubgroups(groupId, pageable)
        );
    }

    @Override
    public ResponseEntity<StudentGroupResponse> update(
        UUID groupId,
        UpdateGroupRequest request
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(
            studentGroupService.update(groupId, request)
        );
    }
}
