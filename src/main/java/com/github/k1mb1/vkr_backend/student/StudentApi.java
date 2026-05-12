package com.github.k1mb1.vkr_backend.student;

import com.github.k1mb1.vkr_backend.student.internal.web.requests.CreateStudentRequest;
import com.github.k1mb1.vkr_backend.student.internal.web.requests.UpdateStudentRequest;
import com.github.k1mb1.vkr_backend.student.internal.web.response.StudentResponse;

import java.util.List;
import java.util.UUID;

public interface StudentApi {
    List<StudentResponse> findActiveStudentsByGroup(UUID groupId);

    List<StudentResponse> findStudentsByGroup(UUID groupId);

    void updateStudent(UUID studentId, UpdateStudentRequest request);

    StudentResponse createStudent(CreateStudentRequest request);

    void archiveStudent(UUID studentId);

    void deleteStudentsByGroup(UUID groupId);
}
