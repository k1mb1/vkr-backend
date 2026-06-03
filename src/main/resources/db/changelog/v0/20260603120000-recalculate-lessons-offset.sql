--liquibase formatted sql

--changeset k1mb1:recalculate-lessons-offset
WITH lessons_with_assignments AS (
    SELECT l.id, l.subject_id, l.lesson_type, l.order_index
    FROM lessons l
    WHERE l.archived_at IS NULL
      AND EXISTS (SELECT 1 FROM assignments a WHERE a.lesson_id = l.id)
),
grades_data AS (
    SELECT
        g.id AS grade_id,
        l_due.subject_id,
        l_due.lesson_type,
        l_awarded.order_index AS awarded_order,
        l_due.order_index   AS due_order
    FROM grades g
    JOIN assignments a ON a.id = g.assignment_id
    JOIN lessons l_due ON l_due.id = a.lesson_id
    JOIN lessons l_awarded ON l_awarded.id = g.awarded_lesson_id
    WHERE g.assignment_id IS NOT NULL
      AND g.awarded_lesson_id IS NOT NULL
),
awarded_ranks AS (
    SELECT gd.grade_id, COUNT(lwa.id) AS rank
    FROM grades_data gd
    LEFT JOIN lessons_with_assignments lwa
           ON lwa.subject_id = gd.subject_id
          AND lwa.lesson_type = gd.lesson_type
          AND lwa.order_index <= gd.awarded_order
    GROUP BY gd.grade_id
),
due_ranks AS (
    SELECT gd.grade_id, COUNT(lwa.id) AS rank
    FROM grades_data gd
    LEFT JOIN lessons_with_assignments lwa
           ON lwa.subject_id = gd.subject_id
          AND lwa.lesson_type = gd.lesson_type
          AND lwa.order_index <= gd.due_order
    GROUP BY gd.grade_id
)
UPDATE grades
SET lessons_offset = ar.rank - dr.rank
FROM awarded_ranks ar
JOIN due_ranks dr ON ar.grade_id = dr.grade_id
WHERE grades.id = ar.grade_id;
--rollback UPDATE grades SET lessons_offset = NULL WHERE assignment_id IS NOT NULL AND awarded_lesson_id IS NOT NULL;
