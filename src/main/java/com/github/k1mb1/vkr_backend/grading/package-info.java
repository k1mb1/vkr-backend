/**
 * Оценки: задания занятий и баллы студентов, таблица оценок с политиками предмета
 * и вкладом посещаемости. Вершина учебного графа перед results: зависит от
 * attendance (сводка), lesson (видимость/ростер/задания — реализует его SPI
 * {@code LessonAssignmentsPort}), subject (политики, права) и group (студенты).
 */
@org.springframework.modulith.ApplicationModule(
        allowedDependencies = {
            "common",
            "auth",
            "attendance :: api",
            "lesson :: api",
            "lesson :: domain",
            "lesson :: specification",
            "subject",
            "subject :: api",
            "subject :: domain",
            "group :: domain"
        })
package com.github.k1mb1.vkr_backend.grading;
