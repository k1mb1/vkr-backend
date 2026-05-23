package com.github.k1mb1.vkr_backend.common.persistence;

import org.springframework.data.jpa.domain.Specification;

/**
 * Переиспользуемые фрагменты JPA Specification.
 */
public final class Specs {

    private Specs() {
    }

    /**
     * Регистронезависимый поиск подстроки в текстовой колонке.
     * Возвращает no-op при null/blank значении — удобно для опциональных фильтров.
     */
    public static <T> Specification<T> containsIgnoreCase(String field, String value) {
        return (root, query, cb) -> {
            if (value == null || value.isBlank()) {
                return null;
            }
            var pattern = "%" + value.toLowerCase() + "%";
            return cb.like(cb.lower(root.get(field)), pattern);
        };
    }
}
