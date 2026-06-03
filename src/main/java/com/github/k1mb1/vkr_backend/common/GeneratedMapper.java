package com.github.k1mb1.vkr_backend.common;

import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Маркер для сгенерированных MapStruct реализаций мапперов.
 * <p>
 * Вешается на интерфейс маппера через {@code @AnnotateWith(GeneratedMapper.class)};
 * MapStruct переносит её на сгенерированный {@code *MapperImpl}. Имя оканчивается на
 * {@code Generated} и retention = CLASS, поэтому аннотация попадает в байт-код и
 * JaCoCo автоматически исключает такой класс из покрытия (AnnotationGeneratedFilter).
 * Это заменяет ручные {@code <excludes>} по имени файла.
 * <p>
 * Стандартный {@code javax.annotation.processing.Generated} от MapStruct имеет
 * retention = SOURCE и в байт-коде отсутствует, поэтому JaCoCo его не видит.
 */
@Retention(CLASS)
@Target({ ElementType.TYPE, ElementType.METHOD })
public @interface GeneratedMapper {
}
