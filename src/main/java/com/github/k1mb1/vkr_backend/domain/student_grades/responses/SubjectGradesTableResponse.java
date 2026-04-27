package com.github.k1mb1.vkr_backend.domain.student_grades.responses;

import com.github.k1mb1.vkr_backend.domain.subjects.responses.SubjectLessonTableEntryResponse;
import java.util.List;

public record SubjectGradesTableResponse(
    List<SubjectLessonTableEntryResponse> lessons,
    List<StudentTaskGradesResponse> students
) {}
