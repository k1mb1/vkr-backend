package com.github.k1mb1.vkr_backend.student_groups;

import com.github.k1mb1.vkr_backend.student_groups.web.filters.StudentGroupFilterRequest;
import com.github.k1mb1.vkr_backend.student_groups.web.responses.StudentGroupListDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StudentGroupsApi {

	Page<StudentGroupListDto> findAll(StudentGroupFilterRequest filter, Pageable pageable);

}
