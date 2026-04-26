package com.github.k1mb1.vkr_backend.domain.lessons;

/**
 * How the displacement coefficient is computed for tasks that have been
 * superseded by a newer task in the same lesson.
 *
 * <p>Let {@code d = lesson.issuedTaskIndex - task.position} (how many steps back
 * this task is from the currently-active task index).
 *
 * <ul>
 *   <li><b>NONE</b> — no penalty; coefficient is always 1.0 regardless of
 *       displacement. This is the default.</li>
 *   <li><b>SUBTRACT</b> — linear subtraction:
 *       {@code coeff = max(0, 1 - penaltyStep * d)}
 *       <br>Example with step=0.25: 1.0, 0.75, 0.5, 0.25, 0.0 …</li>
 *   <li><b>MULTIPLY</b> — geometric decay:
 *       {@code coeff = penaltyStep ^ d}
 *       <br>Example with step=0.5: 1.0, 0.5, 0.25, 0.125 …</li>
 * </ul>
 *
 * <p>The front-end performs the actual calculation; the back-end stores the
 * mode and step so the formula is reproducible without re-sending parameters.
 */
public enum PenaltyMode {
    NONE,
    SUBTRACT,
    MULTIPLY,
}
