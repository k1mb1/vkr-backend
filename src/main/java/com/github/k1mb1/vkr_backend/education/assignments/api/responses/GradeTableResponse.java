package com.github.k1mb1.vkr_backend.education.assignments.api.responses;
import com.github.k1mb1.vkr_backend.education.structure.api.responses.StudentEntryResponse;
import java.util.List;
public record GradeTableResponse(List<TaskResponse> tasks, List<StudentEntryResponse> students, List<GradeCellResponse> grades) {}
