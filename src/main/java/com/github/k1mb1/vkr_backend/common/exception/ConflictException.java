package com.github.k1mb1.vkr_backend.common.exception;

/**
 * Бросать, когда операция конфликтует с текущим состоянием ресурса или
 * нарушает уникальность: сущность уже существует/уже привязана, сессия уже
 * подтверждена/отменена/закрыта и т.п. Маппится в HTTP 409 Conflict.
 *
 * <p>Намеренно отделена от {@link IllegalArgumentException} (некорректный ввод → 400):
 * здесь сам запрос валиден, но противоречит состоянию системы.
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
