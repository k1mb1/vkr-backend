package com.github.k1mb1.vkr_backend.common.exception;

import jakarta.persistence.EntityNotFoundException;

/**
 * Бросать, когда запрошенная по id сущность не найдена.
 * Сообщение формируется единообразно: "<Resource> not found: <id>".
 */
public class ResourceNotFoundException extends EntityNotFoundException {

    private final String resource;

    private final Object id;

    public ResourceNotFoundException(String resource, Object id) {
        super(resource + " not found: " + id);
        this.resource = resource;
        this.id = id;
    }

    public String getResource() {
        return resource;
    }

    public Object getResourceId() {
        return id;
    }
}
