package com.github.k1mb1.vkr_backend.journal.service;

import com.github.k1mb1.vkr_backend.journal.service.dto.response.JournalAudienceScopeResponse;
import com.github.k1mb1.vkr_backend.lesson.domain.LessonEntity;
import com.github.k1mb1.vkr_backend.lesson.domain.LessonScopeEntity;
import com.github.k1mb1.vkr_backend.lesson.specification.LessonSpecifications;
import com.github.k1mb1.vkr_backend.subject.domain.TeacherSubjectPermissionEntity;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Общая для таблиц журнала (посещаемость, оценки) работа с аудиторией права:
 * шапка аудитории и единый порядок видимых scope'ов. Вынесена из двух сервисов,
 * где после слияния модулей дублировалась дословно.
 */
@Service
@Transactional(readOnly = true)
public class JournalAudienceService {

    /** Аудитория права для шапки таблицы (см. {@link LessonSpecifications#audienceScopes}). */
    List<JournalAudienceScopeResponse> audienceOf(TeacherSubjectPermissionEntity permission) {
        return LessonSpecifications.audienceScopes(permission).stream()
                .map(s -> {
                    // audienceScopes() excludes all-groups scopes, so the group is always present.
                    var group = Objects.requireNonNull(s.getGroup());
                    return JournalAudienceScopeResponse.builder()
                            .groupId(group.getId())
                            .groupName(group.getName())
                            .allowedSubgroupId(
                                    s.getAllowedSubgroup() != null
                                            ? s.getAllowedSubgroup().getId()
                                            : null)
                            .allowedSubgroupIndex(
                                    s.getAllowedSubgroup() != null
                                            ? s.getAllowedSubgroup().getIndex()
                                            : null)
                            .build();
                })
                .toList();
    }

    /** Видимые scope'ы занятий в едином для всех таблиц порядке (дата, затем номер занятия). */
    List<LessonScopeEntity> visibleScopesIn(List<LessonEntity> lessons, TeacherSubjectPermissionEntity permission) {
        var result = new ArrayList<LessonScopeEntity>();
        for (var lesson : lessons) {
            result.addAll(LessonSpecifications.visibleScopes(lesson, permission));
        }
        result.sort(Comparator.comparing(
                        (LessonScopeEntity s) -> s.getStartedAt(), Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(s -> s.getLesson().getOrderIndex()));
        return result;
    }
}
