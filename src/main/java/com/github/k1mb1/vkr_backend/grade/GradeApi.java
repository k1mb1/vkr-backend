package com.github.k1mb1.vkr_backend.grade;

import com.github.k1mb1.vkr_backend.grade.web.filters.GradeFilter;
import com.github.k1mb1.vkr_backend.grade.web.requests.UpsertGradeRequest;
import com.github.k1mb1.vkr_backend.grade.web.responses.GradeCellResponse;
import com.github.k1mb1.vkr_backend.grade.web.responses.GradeTableResponse;

public interface GradeApi {
    GradeTableResponse getGradeTable(GradeFilter filter);

    GradeCellResponse upsert(UpsertGradeRequest request);
}
