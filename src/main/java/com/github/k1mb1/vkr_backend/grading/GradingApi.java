package com.github.k1mb1.vkr_backend.grading;

import com.github.k1mb1.vkr_backend.grading.web.filters.GradingFilter;
import com.github.k1mb1.vkr_backend.grading.web.requests.CreateAssignmentsRequest;
import com.github.k1mb1.vkr_backend.grading.web.requests.UpdateAssignmentRequest;
import com.github.k1mb1.vkr_backend.grading.web.requests.UpsertGradeRequest;
import com.github.k1mb1.vkr_backend.grading.web.responses.AssignmentResponse;
import com.github.k1mb1.vkr_backend.grading.web.responses.GradeCellResponse;
import com.github.k1mb1.vkr_backend.grading.web.responses.GradingTableResponse;

import java.util.List;
import java.util.UUID;

public interface GradingApi {
    GradingTableResponse getGradingTable(GradingFilter filter);

    GradeCellResponse upsertGrade(UpsertGradeRequest request);

    List<AssignmentResponse> getAssignmentsByLesson(UUID lessonId);

    List<AssignmentResponse> createAssignments(CreateAssignmentsRequest request);

    AssignmentResponse updateAssignment(UUID id, UpdateAssignmentRequest request);

    void deleteAssignment(UUID id);
}
