package com.github.k1mb1.vkr_backend.attendance;

import com.github.k1mb1.vkr_backend.attendance.web.filters.AttendanceFilter;
import com.github.k1mb1.vkr_backend.attendance.web.requests.BulkUpsertAttendanceRequest;
import com.github.k1mb1.vkr_backend.attendance.web.responses.AttendanceCellResponse;
import com.github.k1mb1.vkr_backend.attendance.web.responses.AttendanceTableResponse;

import java.util.List;

public interface AttendanceApi {
    AttendanceTableResponse getAttendanceTable(AttendanceFilter filter);

    List<AttendanceCellResponse> upsertAll(BulkUpsertAttendanceRequest request);
}
