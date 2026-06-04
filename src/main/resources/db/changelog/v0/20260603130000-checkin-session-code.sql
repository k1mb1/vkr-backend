--liquibase formatted sql

--changeset k1mb1:checkin-session-code
-- Код аудитории для публичной отметки: преподаватель показывает его в аудитории,
-- студент вводит его при check-in. Без кода нельзя отметиться по одному лишь studentId,
-- а массовый перебор/подмена через публичный endpoint затрудняются.
ALTER TABLE check_in_sessions
    ADD COLUMN code VARCHAR(8);
-- Бэкфилл исторических сессий случайным кодом (они уже подтверждены/отменены).
UPDATE check_in_sessions
SET code = upper(substr(md5(random()::text || id::text), 1, 6))
WHERE code IS NULL;
ALTER TABLE check_in_sessions
    ALTER COLUMN code SET NOT NULL;
--rollback ALTER TABLE check_in_sessions DROP COLUMN code;
