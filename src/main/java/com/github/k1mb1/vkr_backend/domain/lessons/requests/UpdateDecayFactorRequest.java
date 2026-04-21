package com.github.k1mb1.vkr_backend.domain.lessons.requests;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * Request body for {@code PATCH /api/lessons/{id}/decay-factor}.
 *
 * @param decayFactor New decay factor for the lesson in the range (0, 1].
 *                    1.0 means no decay; 0.5 halves all task scores from this lesson.
 */
public record UpdateDecayFactorRequest(
    @NotNull @DecimalMin("0.0001") @DecimalMax("1.0") BigDecimal decayFactor
) {}
