package com.github.k1mb1.vkr_backend.attendance.checkin;

import com.github.k1mb1.vkr_backend.attendance.checkin.web.requests.StudentCheckInRequest;
import com.github.k1mb1.vkr_backend.attendance.checkin.web.responses.CheckInRecordResponse;

import java.util.UUID;

public interface CheckInRecordsApi {

    CheckInRecordResponse checkIn(UUID sessionId, StudentCheckInRequest request);
}
