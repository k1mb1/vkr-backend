package com.github.k1mb1.vkr_backend.subject;

import com.github.k1mb1.vkr_backend.subject.web.requests.CreateTeacherSubjectPermissionRequest;
import com.github.k1mb1.vkr_backend.subject.web.requests.UpdateTeacherSubjectPermissionRequest;
import com.github.k1mb1.vkr_backend.subject.web.responses.TeacherSubjectPermissionResponse;
import java.util.List;
import java.util.UUID;

public interface TeacherSubjectPermissionsApi {
    List<TeacherSubjectPermissionResponse> getPermissionsBySubject(UUID subjectId);

    TeacherSubjectPermissionResponse getPermission(UUID subjectId, UUID teacherId);

    TeacherSubjectPermissionResponse create(CreateTeacherSubjectPermissionRequest request);

    TeacherSubjectPermissionResponse update(UUID id, UpdateTeacherSubjectPermissionRequest request);

    void delete(UUID id);
}
