package com.github.k1mb1.vkr_backend.domain.student_groups;

import com.github.k1mb1.vkr_backend.apis.StudentGroupApi;
import com.github.k1mb1.vkr_backend.domain.student_groups.requests.CreateStudentGroupRequest;
import com.github.k1mb1.vkr_backend.domain.student_groups.requests.UpdateStudentGroupRequest;
import com.github.k1mb1.vkr_backend.domain.student_groups.responses.StudentGroupDetailResponse;
import com.github.k1mb1.vkr_backend.domain.student_groups.responses.StudentGroupResponse;
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

    private final StudentGroupService groupService;
}
