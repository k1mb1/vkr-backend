/**
 * Итоги семестра: read-only агрегатор, отдающий оценки и посещаемость одним
 * запросом через опубликованные порты grading и attendance. Вершина модульного
 * графа — от results не зависит никто.
 */
@org.springframework.modulith.ApplicationModule(allowedDependencies = {"common", "attendance :: api", "grading :: api"})
package com.github.k1mb1.vkr_backend.results;
