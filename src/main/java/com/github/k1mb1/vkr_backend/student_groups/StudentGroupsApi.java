package com.github.k1mb1.vkr_backend.student_groups;

import com.github.k1mb1.vkr_backend.student_groups.web.filters.StudentGroupFilterRequest;
import com.github.k1mb1.vkr_backend.student_groups.web.requests.CreateGroupRequest;
import com.github.k1mb1.vkr_backend.student_groups.web.requests.UpdateGroupRequest;
import com.github.k1mb1.vkr_backend.student_groups.web.responses.GroupResponse;
import com.github.k1mb1.vkr_backend.student_groups.web.responses.StudentGroupListDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface StudentGroupsApi {

	Page<StudentGroupListDto> findAll(StudentGroupFilterRequest filter, Pageable pageable);

	GroupResponse findById(UUID id);

	GroupResponse create(CreateGroupRequest request);

	GroupResponse patch(UUID id, UpdateGroupRequest request);

}
