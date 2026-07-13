package com.github.k1mb1.vkr_backend.group.api;

import java.util.List;
import java.util.UUID;

/**
 * SPI модуля group: операции над связью «предмет — группы», которой владеет модуль
 * subject. Реализуется subject'ом (subject уже зависит от group), так что group
 * может обслуживать свои эндпоинты привязки, не зная о существовании subject.
 */
public interface GroupSubjectsPort {

    /** id групп, привязанных к предмету. */
    List<UUID> groupIdsOfSubject(UUID subjectId);

    /** Привязывает группу к предмету; конфликт, если уже привязана. */
    void attachGroup(UUID groupId, UUID subjectId);

    /** Отвязывает группу от предмета; ошибка, если не была привязана. */
    void detachGroup(UUID groupId, UUID subjectId);
}
