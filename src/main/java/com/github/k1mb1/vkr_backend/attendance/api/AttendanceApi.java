package com.github.k1mb1.vkr_backend.attendance.api;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * Публичный порт модуля attendance. Параметры — скаляры (id), чтобы не
 * публиковать внутренние фильтры модуля; таблица и сводка — records этого пакета.
 */
public interface AttendanceApi {

    /** Таблица посещаемости под разрешением; scope/lesson опционально сужают выборку. */
    AttendanceTableResponse getAttendanceTable(
            UUID permissionId, @Nullable UUID lessonScopeId, @Nullable UUID lessonId);

    /** Сводка посещаемости (кол-во по статусам) по студентам для набора проведений. */
    Map<UUID, AttendanceSummaryResponse> summarize(Collection<UUID> lessonScopeIds, Collection<UUID> studentIds);
}
