package com.github.k1mb1.vkr_backend.subject.api;

import java.util.Set;
import java.util.UUID;

/**
 * Порт, объявленный subject и реализуемый teacher (инверсия зависимости): какие
 * предметы видны преподавателю (те, на которые у него есть право). subject
 * фильтрует свой список по этим id, не заглядывая в таблицу прав.
 */
public interface SubjectVisibilityPort {

    /** id предметов, на которые у преподавателя есть выданное право. */
    Set<UUID> visibleSubjectIds(UUID teacherId);
}
