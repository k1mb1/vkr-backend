/**
 * Предметы и их политики (посещаемость, оценки, итоги, check-in), справочник
 * преподавателей и выданные им права с ограничением аудитории (право — связь
 * «преподаватель × предмет», поэтому справочник живёт здесь же). Владеет словарём
 * {@code LessonType} (в корне модуля) и связью «предмет — группы». Зависит от
 * справочника group и реализует SPI-порты auth и group.
 */
@org.springframework.modulith.ApplicationModule(
        allowedDependencies = {"common", "auth", "auth :: api", "group :: api", "group :: domain"})
package com.github.k1mb1.vkr_backend.subject;
