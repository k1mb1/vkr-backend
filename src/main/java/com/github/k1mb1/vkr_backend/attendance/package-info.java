/**
 * Посещаемость: отметки по студентам на проведениях занятий + подмодуль check-in
 * (QR-самоотметка студентов, вложенный пакет). Строится над lesson (ростер и
 * видимость), публикует таблицу и сводку для grading/results; реализует SPI auth
 * для проверки доступа к check-in сессиям.
 */
@org.springframework.modulith.ApplicationModule(
        allowedDependencies = {
            "common",
            "auth :: api",
            "lesson :: api",
            "lesson :: domain",
            "lesson :: specification",
            "subject",
            "subject :: api",
            "subject :: domain",
            "group :: domain"
        })
package com.github.k1mb1.vkr_backend.attendance;
