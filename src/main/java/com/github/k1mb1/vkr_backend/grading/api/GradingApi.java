package com.github.k1mb1.vkr_backend.grading.api;

import com.github.k1mb1.vkr_backend.grading.service.dto.filter.GradingFilter;
import com.github.k1mb1.vkr_backend.grading.service.dto.request.BulkUpdateAssignmentsRequest;
import com.github.k1mb1.vkr_backend.grading.service.dto.request.BulkUpsertGradesRequest;
import com.github.k1mb1.vkr_backend.grading.service.dto.request.CreateAssignmentsRequest;
import com.github.k1mb1.vkr_backend.grading.service.dto.response.AssignmentResponse;
import com.github.k1mb1.vkr_backend.grading.service.dto.response.GradeCellResponse;
import com.github.k1mb1.vkr_backend.grading.service.dto.response.GradingTableResponse;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface GradingApi {
    GradingTableResponse getGradingTable(GradingFilter filter);

    List<GradeCellResponse> upsertGrades(BulkUpsertGradesRequest request);

    List<AssignmentResponse> getAssignmentsByLesson(UUID lessonId);

    Map<UUID, List<AssignmentResponse>> getAssignmentsByLessons(Collection<UUID> lessonIds);

    List<AssignmentResponse> createAssignments(CreateAssignmentsRequest request);

    List<AssignmentResponse> updateAssignmentsOfLesson(UUID lessonId, BulkUpdateAssignmentsRequest request);

    void deleteAssignment(UUID id);

    void deleteAssignmentsOfLesson(UUID lessonId);
}
