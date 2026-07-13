/**
 * Контингент: группы, подгруппы и студенты — один агрегат ростера (студент всегда
 * принадлежит группе, ростер управляется через эндпоинты групп). Низ графа: от group
 * зависят subject/lesson/attendance/grading; связью «предмет—группы» владеет subject
 * и реализует SPI {@code group.api.GroupSubjectsPort}.
 */
@org.springframework.modulith.ApplicationModule(allowedDependencies = {"common"})
package com.github.k1mb1.vkr_backend.group;
