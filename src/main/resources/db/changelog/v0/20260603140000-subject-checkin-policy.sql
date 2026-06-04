--liquibase formatted sql

--changeset k1mb1:subject-checkin-policy
-- Единое время на отметку (check-in) для всего предмета (опциональная фича).
-- Если checkin_enabled=true, окна сессий берутся отсюда, а не из запроса на запуск.
ALTER TABLE subjects
    ADD COLUMN checkin_enabled         BOOLEAN NOT NULL DEFAULT false,
    ADD COLUMN checkin_on_time_seconds INTEGER CHECK (checkin_on_time_seconds IS NULL OR checkin_on_time_seconds > 0),
    ADD COLUMN checkin_late_seconds    INTEGER CHECK (checkin_late_seconds IS NULL OR checkin_late_seconds >= 0);
--rollback ALTER TABLE subjects DROP COLUMN checkin_enabled, DROP COLUMN checkin_on_time_seconds, DROP COLUMN checkin_late_seconds;
