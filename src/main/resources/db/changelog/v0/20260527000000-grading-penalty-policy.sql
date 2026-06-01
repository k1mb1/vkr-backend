--liquibase formatted sql

--changeset k1mb1:048-subject-penalty-policy
-- Параметры понижения балла за просрочку (опциональная фича).
-- Расчёт итогового балла выполняется на фронте — здесь только конфигурация.
ALTER TABLE subjects
    ADD COLUMN penalty_enabled              BOOLEAN       NOT NULL DEFAULT false,
    ADD COLUMN penalty_operation            VARCHAR(16),
    ADD COLUMN penalty_step                 NUMERIC(6, 3),
    ADD COLUMN penalty_grace_period_lessons INTEGER,
    ADD COLUMN penalty_interval_lessons     INTEGER,
    ADD COLUMN penalty_max_reductions       INTEGER;
ALTER TABLE subjects
    ADD CONSTRAINT chk_subjects_penalty_operation
        CHECK (penalty_operation IS NULL OR penalty_operation IN ('SUBTRACT', 'MULTIPLY'));
-- Когда фича включена — все параметры обязательны и валидны.
ALTER TABLE subjects
    ADD CONSTRAINT chk_subjects_penalty_enabled_complete
        CHECK (
            penalty_enabled = false
                OR (penalty_operation IS NOT NULL
                AND penalty_step IS NOT NULL AND penalty_step > 0
                AND penalty_grace_period_lessons IS NOT NULL AND penalty_grace_period_lessons >= 0
                AND penalty_interval_lessons IS NOT NULL AND penalty_interval_lessons >= 1
                AND penalty_max_reductions IS NOT NULL AND penalty_max_reductions >= 1)
            );
--rollback ALTER TABLE subjects DROP CONSTRAINT chk_subjects_penalty_enabled_complete;
--rollback ALTER TABLE subjects DROP CONSTRAINT chk_subjects_penalty_operation;
--rollback ALTER TABLE subjects DROP COLUMN penalty_enabled, DROP COLUMN penalty_operation, DROP COLUMN penalty_step, DROP COLUMN penalty_grace_period_lessons, DROP COLUMN penalty_interval_lessons, DROP COLUMN penalty_max_reductions;

--changeset k1mb1:049-lesson-active-marker
-- Маркер активного (текущего) занятия — точка отсчёта для понижения балла.
ALTER TABLE lessons
    ADD COLUMN active BOOLEAN NOT NULL DEFAULT false;
-- Не более одного активного занятия на предмет (среди неархивных).
CREATE UNIQUE INDEX uk_lessons_active_per_subject
    ON lessons (subject_id) WHERE active = true AND archived_at IS NULL;
--rollback DROP INDEX IF EXISTS uk_lessons_active_per_subject;
--rollback ALTER TABLE lessons DROP COLUMN active;

--changeset k1mb1:050-grade-awarded-lesson
-- Точка отсчёта понижения на уровне ячейки: занятие, на момент которого
-- оценка выставлена (= активное занятие предмета при создании). Снижение
-- считается на фронте по каждой ячейке отдельно (per-assignment, не на всю группу).
ALTER TABLE grades
    ADD COLUMN awarded_lesson_id UUID REFERENCES lessons (id) ON DELETE SET NULL;
CREATE INDEX idx_grades_awarded_lesson_id ON grades (awarded_lesson_id);
--rollback DROP INDEX IF EXISTS idx_grades_awarded_lesson_id;
--rollback ALTER TABLE grades DROP COLUMN awarded_lesson_id;
