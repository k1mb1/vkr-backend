package com.github.k1mb1.vkr_backend.teacher;

import com.github.k1mb1.vkr_backend.teacher.web.filters.TeacherFilter;
import com.github.k1mb1.vkr_backend.teacher.web.requests.CreateOrUpdateTeacherRequest;
import com.github.k1mb1.vkr_backend.teacher.web.response.TeacherResponse;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TeachersApi {

    TeacherResponse createOrUpdateTeacher(UUID id, CreateOrUpdateTeacherRequest request);

    Page<TeacherResponse> getPage(TeacherFilter filter, Pageable pageable);
}
