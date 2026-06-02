package com.github.k1mb1.vkr_backend.support;

import com.github.k1mb1.vkr_backend.common.error.GlobalExceptionHandler;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.client.RestTestClient;

/**
 * Утилита для контроллерных тестов на {@link RestTestClient#bindToController}.
 * <p>
 * Standalone-режим не поднимает Spring-контекст, поэтому вручную регистрируем то,
 * что реально влияет на поведение эндпоинтов: резолвер {@code Pageable} (Spring Data)
 * и реальный {@link GlobalExceptionHandler}, чтобы исключения мапились в те же
 * HTTP-статусы, что и в проде. Бин-валидация подключается standalone-builder'ом сама.
 */
public final class ControllerTestSupport {

    private ControllerTestSupport() {
    }

    public static RestTestClient client(Object controller) {
        return RestTestClient.bindToController(controller)
            .configureServer(builder -> builder
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver()))
            .build();
    }
}
