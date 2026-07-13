package com.github.k1mb1.vkr_backend.grading.api;

import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * Публичный порт модуля grading. Параметры — скаляры (id), чтобы не публиковать
 * внутренние фильтры модуля; таблица — records этого пакета.
 */
public interface GradingApi {

    /** Таблица оценок под разрешением; scope/lesson опционально сужают выборку. */
    GradingTableResponse getGradingTable(UUID permissionId, @Nullable UUID lessonScopeId, @Nullable UUID lessonId);
}
