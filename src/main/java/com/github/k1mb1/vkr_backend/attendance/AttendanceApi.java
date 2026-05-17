package com.github.k1mb1.vkr_backend.attendance;

import com.github.k1mb1.vkr_backend.attendance.web.filters.AttendanceFilter;
import com.github.k1mb1.vkr_backend.attendance.web.requests.UpsertAttendanceRequest;
import com.github.k1mb1.vkr_backend.attendance.web.responses.AttendanceCellResponse;
import com.github.k1mb1.vkr_backend.attendance.web.responses.AttendanceTableResponse;

public interface AttendanceApi {
    AttendanceTableResponse getAttendanceTable(AttendanceFilter filter);

    AttendanceCellResponse upsert(UpsertAttendanceRequest request);
}
