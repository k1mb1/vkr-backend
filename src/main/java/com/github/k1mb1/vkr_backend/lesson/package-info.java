/**
 * Занятия и их проведения (scope: аудитория «группа/подгруппа/все группы» + дата).
 * Ядро учебного процесса: journal строится над ним. Публикует DTO-порт ростера
 * аудитории ({@code LessonStudentsApi}) и порт заданий ({@code LessonAssignmentsPort},
 * реализует journal); реализует авторизацию занятий.
 */
package com.github.k1mb1.vkr_backend.lesson;
