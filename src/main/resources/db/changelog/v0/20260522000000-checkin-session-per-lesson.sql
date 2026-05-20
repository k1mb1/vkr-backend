--liquibase formatted sql

--changeset k1mb1:030-checkin-session-per-lesson
DROP INDEX IF EXISTS idx_check_in_sessions_lesson_scope;
ALTER TABLE check_in_sessions
DROP
COLUMN lesson_scope_id;
CREATE UNIQUE INDEX uk_check_in_sessions_active_per_lesson
    ON check_in_sessions (lesson_id) WHERE confirmed_at IS NULL AND cancelled_at IS NULL;
--rollback DROP INDEX IF EXISTS uk_check_in_sessions_active_per_lesson;
--rollback ALTER TABLE check_in_sessions ADD COLUMN lesson_scope_id UUID REFERENCES lesson_scopes (id) ON DELETE CASCADE;
--rollback CREATE INDEX idx_check_in_sessions_lesson_scope ON check_in_sessions (lesson_scope_id);
