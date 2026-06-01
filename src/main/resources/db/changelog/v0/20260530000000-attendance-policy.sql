--liquibase formatted sql

--changeset k1mb1:055-subject-attendance-policy
-- Связка посещаемости с баллом (опциональная фича). Расчёт вклада — на фронте.
ALTER TABLE subjects
    ADD COLUMN attendance_enabled        BOOLEAN       NOT NULL DEFAULT false,
    ADD COLUMN attendance_points_present NUMERIC(6, 3) NOT NULL DEFAULT 0,
    ADD COLUMN attendance_points_late    NUMERIC(6, 3) NOT NULL DEFAULT 0,
    ADD COLUMN attendance_points_absent  NUMERIC(6, 3) NOT NULL DEFAULT 0,
    ADD COLUMN attendance_points_excused NUMERIC(6, 3) NOT NULL DEFAULT 0;
--rollback ALTER TABLE subjects DROP COLUMN attendance_enabled, DROP COLUMN attendance_points_present, DROP COLUMN attendance_points_late, DROP COLUMN attendance_points_absent, DROP COLUMN attendance_points_excused;
