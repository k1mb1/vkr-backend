--liquibase formatted sql

--changeset k1mb1:053-grade-lessons-offset
-- lessons_late (>=0) становится знаковым lessons_offset: >0 позже срока, <0 раньше.
ALTER TABLE grades DROP CONSTRAINT IF EXISTS grades_lessons_late_check;
ALTER TABLE grades RENAME COLUMN lessons_late TO lessons_offset;
--rollback ALTER TABLE grades RENAME COLUMN lessons_offset TO lessons_late;
--rollback ALTER TABLE grades ADD CONSTRAINT grades_lessons_late_check CHECK (lessons_late IS NULL OR lessons_late >= 0);

--changeset k1mb1:054-subject-bonus-policy
-- Бонус за раннюю сдачу (опциональная фича), независим от понижения. Расчёт на фронте.
ALTER TABLE subjects
    ADD COLUMN bonus_enabled              BOOLEAN       NOT NULL DEFAULT false,
    ADD COLUMN bonus_operation            VARCHAR(16),
    ADD COLUMN bonus_step                 NUMERIC(6, 3),
    ADD COLUMN bonus_grace_period_lessons INTEGER,
    ADD COLUMN bonus_interval_lessons     INTEGER,
    ADD COLUMN bonus_max_increases        INTEGER;
ALTER TABLE subjects
    ADD CONSTRAINT chk_subjects_bonus_operation
        CHECK (bonus_operation IS NULL OR bonus_operation IN ('ADD', 'MULTIPLY'));
ALTER TABLE subjects
    ADD CONSTRAINT chk_subjects_bonus_enabled_complete
        CHECK (
            bonus_enabled = false
                OR (bonus_operation IS NOT NULL
                AND bonus_step IS NOT NULL AND bonus_step > 0
                AND bonus_grace_period_lessons IS NOT NULL AND bonus_grace_period_lessons >= 0
                AND bonus_interval_lessons IS NOT NULL AND bonus_interval_lessons >= 1
                AND bonus_max_increases IS NOT NULL AND bonus_max_increases >= 1)
            );
--rollback ALTER TABLE subjects DROP CONSTRAINT chk_subjects_bonus_enabled_complete;
--rollback ALTER TABLE subjects DROP CONSTRAINT chk_subjects_bonus_operation;
--rollback ALTER TABLE subjects DROP COLUMN bonus_enabled, DROP COLUMN bonus_operation, DROP COLUMN bonus_step, DROP COLUMN bonus_grace_period_lessons, DROP COLUMN bonus_interval_lessons, DROP COLUMN bonus_max_increases;
