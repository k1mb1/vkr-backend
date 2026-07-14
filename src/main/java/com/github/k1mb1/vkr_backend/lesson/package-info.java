/**
 * Занятия и их проведения (scope: аудитория «группа/подгруппа/все группы» + дата).
 * Ядро учебного процесса: journal строится над ним. Публикует DTO-порт
 * ростера аудитории ({@code LessonStudentsApi}) и SPI заданий
 * ({@code LessonAssignmentsPort}, реализует journal); реализует SPI auth.
 */
@org.springframework.modulith.ApplicationModule(
        allowedDependencies = {"common", "auth :: api", "subject", "subject :: domain", "group :: domain"})
package com.github.k1mb1.vkr_backend.lesson;
