package com.github.k1mb1.vkr_backend.common.web;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Машинно-читаемый код ошибки")
public enum ErrorCode {
    NOT_FOUND,
    VALIDATION_FAILED,
    MALFORMED_BODY,
    INVALID_PARAM,
    ACCESS_DENIED,
    ILLEGAL_ARGUMENT,
    ILLEGAL_STATE,
    CONFLICT,
    INTERNAL_ERROR,
}
