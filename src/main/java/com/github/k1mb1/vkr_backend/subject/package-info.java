/**
 * Предметы и их политики (посещаемость, оценки, итоги, check-in) плюс выданные
 * преподавателям права с ограничением аудитории. Владеет словарём {@code LessonType}
 * (в корне модуля) и связью «предмет — группы». Зависит от справочников group/teacher
 * и реализует SPI-порты auth и group.
 */
@org.springframework.modulith.ApplicationModule(
        allowedDependencies = {"common", "auth", "auth :: api", "group :: api", "group :: domain", "teacher :: domain"})
package com.github.k1mb1.vkr_backend.subject;
