package com.github.k1mb1.vkr_backend.attendance.api;

import com.github.k1mb1.vkr_backend.attendance.service.dto.filter.AttendanceFilter;
import com.github.k1mb1.vkr_backend.attendance.service.dto.request.BulkUpsertAttendanceRequest;
import com.github.k1mb1.vkr_backend.attendance.service.dto.response.AttendanceCellResponse;
import com.github.k1mb1.vkr_backend.attendance.service.dto.response.AttendanceTableResponse;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface AttendanceApi {
    AttendanceTableResponse getAttendanceTable(AttendanceFilter filter);

    List<AttendanceCellResponse> upsertAll(BulkUpsertAttendanceRequest request);

    /** Сводка посещаемости (кол-во по статусам) по студентам для набора проведений. */
    Map<UUID, AttendanceSummary> summarize(Collection<UUID> lessonScopeIds, Collection<UUID> studentIds);
}
