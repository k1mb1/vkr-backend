package com.github.k1mb1.vkr_backend.attendance.checkin;

import com.github.k1mb1.vkr_backend.attendance.checkin.web.filters.CheckInSessionFilter;
import com.github.k1mb1.vkr_backend.attendance.checkin.web.requests.ConfirmCheckInRequest;
import com.github.k1mb1.vkr_backend.attendance.checkin.web.requests.StartCheckInRequest;
import com.github.k1mb1.vkr_backend.attendance.checkin.web.responses.CheckInPreviewResponse;
import com.github.k1mb1.vkr_backend.attendance.checkin.web.responses.CheckInSessionResponse;
import com.github.k1mb1.vkr_backend.attendance.checkin.web.responses.PublicCheckInSessionResponse;

import java.util.List;
import java.util.UUID;

public interface CheckInSessionApi {

    CheckInSessionResponse start(StartCheckInRequest request);

    CheckInSessionResponse get(UUID sessionId);

    List<CheckInSessionResponse> list(CheckInSessionFilter filter);

    CheckInPreviewResponse preview(UUID sessionId);

    CheckInSessionResponse confirm(UUID sessionId, ConfirmCheckInRequest request);

    CheckInSessionResponse cancel(UUID sessionId);

    PublicCheckInSessionResponse getPublic(UUID sessionId);
}
