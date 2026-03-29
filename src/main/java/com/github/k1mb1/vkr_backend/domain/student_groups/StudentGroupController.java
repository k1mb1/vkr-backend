package com.github.k1mb1.vkr_backend.domain.student_groups;

import com.github.k1mb1.vkr_backend.apis.StudentGroupApi;
import com.github.k1mb1.vkr_backend.domain.student_groups.responses.StudentGroupResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class StudentGroupController implements StudentGroupApi {

    final StudentGroupService studentGroupService;

    @Override
    public ResponseEntity<StudentGroupResponse> findGroupWithSubgroups(UUID groupId) {
        return ResponseEntity.status(HttpStatus.OK).body(
            studentGroupService.findGroupWithSubgroups(groupId)
        );
    }
}
