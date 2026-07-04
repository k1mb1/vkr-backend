package com.github.k1mb1.vkr_backend.grading;

import com.github.k1mb1.vkr_backend.grading.web.filters.GradingFilter;
import com.github.k1mb1.vkr_backend.grading.web.requests.BulkUpdateAssignmentsRequest;
import com.github.k1mb1.vkr_backend.grading.web.requests.BulkUpsertGradesRequest;
import com.github.k1mb1.vkr_backend.grading.web.requests.CreateAssignmentsRequest;
import com.github.k1mb1.vkr_backend.grading.web.responses.AssignmentResponse;
import com.github.k1mb1.vkr_backend.grading.web.responses.GradeCellResponse;
import com.github.k1mb1.vkr_backend.grading.web.responses.GradingTableResponse;
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
