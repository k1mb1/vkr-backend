/**
 * Журнал: отметки посещаемости, check-in (QR-самоотметка, вложенный пакет
 * {@code checkin}), задания/оценки и итоги семестра. Один модуль — потому что всё
 * это один домен: сетка «студенты × проведения занятий» с общей моделью
 * видимости под правом преподавателя, общими справочными репозиториями и одним
 * резолвером занятий. Зависит от lesson (видимость, ростер, SPI заданий),
 * subject (политики, права), group (студенты) и реализует SPI auth для
 * check-in сессий. Вершина модульного графа — от journal не зависит никто.
 */
@org.springframework.modulith.ApplicationModule(
        allowedDependencies = {
            "common",
            "auth",
            "auth :: api",
            "lesson :: api",
            "lesson :: domain",
            "lesson :: specification",
            "subject",
            "subject :: api",
            "subject :: domain",
            "group :: domain"
        })
package com.github.k1mb1.vkr_backend.journal;
