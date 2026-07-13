/**
 * Спецификации видимости занятий под правом преподавателя. Опубликованы отдельным
 * интерфейсом: attendance/grading строят свои таблицы по тем же правилам видимости,
 * что и сам lesson, — единое место истины для «что видно под разрешением».
 */
@org.springframework.modulith.NamedInterface("specification")
package com.github.k1mb1.vkr_backend.lesson.specification;
