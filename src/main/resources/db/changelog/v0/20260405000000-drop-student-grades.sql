-- Drop the old flat student_grades table.
-- Grades are now fully covered by student_task_grades (per-task, with status,
-- deadline and displacement coefficients). student_grades was a denormalised
-- "one number per lesson" shortcut that is no longer needed.

ALTER TABLE lessons
    DROP COLUMN IF EXISTS student_grades_placeholder; -- safety no-op placeholder

DROP TABLE IF EXISTS student_grades CASCADE;
