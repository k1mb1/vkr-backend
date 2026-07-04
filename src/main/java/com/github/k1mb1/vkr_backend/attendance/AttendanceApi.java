package com.github.k1mb1.vkr_backend.attendance;

import com.github.k1mb1.vkr_backend.attendance.web.filters.AttendanceFilter;
import com.github.k1mb1.vkr_backend.attendance.web.requests.BulkUpsertAttendanceRequest;
import com.github.k1mb1.vkr_backend.attendance.web.responses.AttendanceCellResponse;
import com.github.k1mb1.vkr_backend.attendance.web.responses.AttendanceTableResponse;
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
