package com.github.k1mb1.vkr_backend.student;

import com.github.k1mb1.vkr_backend.student.internal.web.response.StudentResponse;
import java.util.List;
import java.util.UUID;

public interface StudentApi {
    List<StudentResponse> findActiveByGroup(UUID groupId);

    List<StudentResponse> findByGroup(UUID groupId);

    void archive(UUID studentId);

    void update(UUID studentId, String username, UUID subgroupId);

    StudentResponse create(String username, UUID groupId, UUID subgroupId);

    void deleteByGroup(UUID groupId);
}
