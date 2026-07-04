package com.github.k1mb1.vkr_backend.attendance.checkin;

import com.github.k1mb1.vkr_backend.attendance.checkin.web.filters.CheckInSessionFilter;
import com.github.k1mb1.vkr_backend.attendance.checkin.web.requests.ConfirmCheckInRequest;
import com.github.k1mb1.vkr_backend.attendance.checkin.web.requests.StartCheckInRequest;
import com.github.k1mb1.vkr_backend.attendance.checkin.web.responses.CheckInPreviewResponse;
import com.github.k1mb1.vkr_backend.attendance.checkin.web.responses.CheckInSessionResponse;
import com.github.k1mb1.vkr_backend.attendance.checkin.web.responses.PublicCheckInSessionResponse;
import com.github.k1mb1.vkr_backend.attendance.checkin.web.responses.PublicStudentResponse;
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

    /**
     * Проверяет код аудитории сессии (шаг «подтвердить код» перед поиском).
     * Бросает исключение, если сессия закрыта или код неверный.
     */
    void verifyCode(UUID sessionId, String code);

    /**
     * Поиск студента сессии по фамилии (части ФИО) для публичной страницы check-in.
     * <p>
     * Доступ за кодом аудитории: сначала проверяется {@code code}, и только при совпадении
     * выполняется поиск. В отличие от полного ростера, отдаёт только совпадения с запросом,
     * без статусов посещаемости — чтобы список группы и сведения о том, кто пришёл, не были
     * доступны без кода и не скрейпились одним запросом. Слишком короткий или пустой запрос,
     * а также неоднозначный запрос (найдено 0 или более 1 студента), возвращает пустой список.
     */
    List<PublicStudentResponse> searchStudents(UUID sessionId, String code, String query);
}
