package com.github.k1mb1.vkr_backend.student;

import com.github.k1mb1.vkr_backend.student.internal.web.requests.CreateStudentRequest;
import com.github.k1mb1.vkr_backend.student.internal.web.requests.UpdateStudentRequest;
import com.github.k1mb1.vkr_backend.student.internal.web.response.StudentResponse;
import java.util.List;
import java.util.UUID;

public interface StudentApi {
    List<StudentResponse> findActiveByGroup(UUID groupId);

    List<StudentResponse> findByGroup(UUID groupId);

    void update(UUID studentId, UpdateStudentRequest request);

    StudentResponse create(CreateStudentRequest request);

    void archive(UUID studentId);

    void deleteByGroup(UUID groupId);
}
