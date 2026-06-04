package com.github.k1mb1.vkr_backend.attendance.checkin;

import com.github.k1mb1.vkr_backend.attendance.checkin.web.requests.StudentCheckInRequest;
import com.github.k1mb1.vkr_backend.attendance.checkin.web.responses.PublicCheckInRecordResponse;

import java.util.UUID;

public interface CheckInRecordsApi {

    PublicCheckInRecordResponse checkIn(UUID sessionId, StudentCheckInRequest request);
}
