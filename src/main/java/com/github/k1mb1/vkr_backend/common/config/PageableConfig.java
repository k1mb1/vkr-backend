package com.github.k1mb1.vkr_backend.common.config;

import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Кап на размер страницы {@code Pageable}: без него клиент мог запросить
 * {@code ?size=10000000} и выкачать всю таблицу за один запрос (DoS на списковые эндпоинты).
 * Spring перенаправит любой размер сверх лимита на {@link #MAX_PAGE_SIZE}, не падая с ошибкой.
 *
 * <p>WebMvcConfigurer.addArgumentResolvers помещает наш резолвер перед дефолтным
 * из Spring Boot, так что кап гарантированно применяется ко всем {@code Pageable}-параметрам.
 */
@Configuration
public class PageableConfig implements WebMvcConfigurer {

    static final int MAX_PAGE_SIZE = 200;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        PageableHandlerMethodArgumentResolver pageableResolver = new PageableHandlerMethodArgumentResolver();
        pageableResolver.setMaxPageSize(MAX_PAGE_SIZE);
        resolvers.add(pageableResolver);
    }
}
