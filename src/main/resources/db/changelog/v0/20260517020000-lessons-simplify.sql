--liquibase formatted sql

--changeset k1mb1:010-lessons-simplify splitStatements:true endDelimiter:;
ALTER TABLE lessons
DROP
CONSTRAINT IF EXISTS chk_lesson_time;
ALTER TABLE lessons
DROP
COLUMN IF EXISTS ended_at;
ALTER TABLE lessons
DROP
COLUMN IF EXISTS teacher_id;
ALTER TABLE lessons
ALTER
COLUMN started_at TYPE DATE USING (started_at AT TIME ZONE 'UTC')::date;
--rollback ALTER TABLE lessons ALTER COLUMN started_at TYPE TIMESTAMPTZ USING (started_at::timestamp AT TIME ZONE 'UTC');
--rollback ALTER TABLE lessons ADD COLUMN teacher_id UUID REFERENCES teachers (id) ON DELETE SET NULL;
--rollback CREATE INDEX idx_lessons_teacher_id ON lessons (teacher_id);
--rollback ALTER TABLE lessons ADD COLUMN ended_at TIMESTAMPTZ;
--rollback ALTER TABLE lessons ADD CONSTRAINT chk_lesson_time CHECK (ended_at IS NULL OR ended_at > started_at);
