package com.github.k1mb1.vkr_backend.domain.student_attendances.responses;

import com.github.k1mb1.vkr_backend.domain.subjects.responses.SubjectLessonTableEntryResponse;
import java.util.List;

public record SubjectAttendanceTableResponse(
    List<SubjectLessonTableEntryResponse> lessons,
    List<StudentAttendanceTableResponse> students
) {}
