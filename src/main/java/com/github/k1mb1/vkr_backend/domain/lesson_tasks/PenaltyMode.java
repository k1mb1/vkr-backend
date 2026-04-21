package com.github.k1mb1.vkr_backend.domain.lesson_tasks;

/**
 * How the displacement coefficient is computed for tasks that have been
 * superseded by a newer (more up-to-date) task in the same lesson.
 *
 * <p>Let {@code d = issued_task_index - task.position} (how many steps back
 * this task is from the currently-active task index).
 *
 * <ul>
 *   <li><b>SUBTRACT</b> — linear subtraction:
 *       {@code coeff = max(0, 1 - penalty_step * d)}
 *       <br>Example with step=0.25: 1.0, 0.75, 0.5, 0.25, 0.0 …</li>
 *   <li><b>MULTIPLY</b> — geometric decay:
 *       {@code coeff = penalty_step ^ d}
 *       <br>Example with step=0.5: 1.0, 0.5, 0.25, 0.125 …</li>
 * </ul>
 *
 * <p>The front-end performs the actual calculation; the back-end stores the
 * mode and step so the formula is reproducible without re-sending parameters.
 */
public enum PenaltyMode {
    /**
     * Subtract {@code penalty_step} for each step back from the active task.
     * Clamped at 0.
     */
    SUBTRACT,

    /**
     * Multiply by {@code penalty_step} for each step back from the active task.
     * Approaches zero geometrically.
     */
    MULTIPLY,
}
