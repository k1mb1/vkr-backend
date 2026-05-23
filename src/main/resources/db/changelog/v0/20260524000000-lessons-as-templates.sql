--liquibase formatted sql

--changeset k1mb1:040-lesson-scopes-started-at
ALTER TABLE lesson_scopes
    ADD COLUMN started_at DATE,
    ADD COLUMN all_groups BOOLEAN NOT NULL DEFAULT false;
--rollback ALTER TABLE lesson_scopes DROP COLUMN started_at;
--rollback ALTER TABLE lesson_scopes DROP COLUMN all_groups;

--changeset k1mb1:041-backfill-lesson-scopes-from-lessons
UPDATE lesson_scopes ls
SET started_at = l.started_at,
    all_groups = l.all_groups FROM lessons l
WHERE ls.lesson_id = l.id;
--rollback SELECT 1;

--changeset k1mb1:041b-make-lesson-scope-group-nullable
ALTER TABLE lesson_scopes
    ALTER COLUMN group_id DROP NOT NULL;
--rollback ALTER TABLE lesson_scopes ALTER COLUMN group_id SET NOT NULL;

--changeset k1mb1:042-create-all-groups-scopes-for-orphan-lessons
INSERT INTO lesson_scopes (id, lesson_id, group_id, allowed_subgroup_id, started_at, all_groups,
                           created_at, updated_at)
SELECT gen_random_uuid(),
       l.id,
       NULL,
       NULL,
       l.started_at,
       TRUE,
       l.created_at,
       l.updated_at
FROM lessons l
WHERE l.archived_at IS NULL
  AND l.all_groups = TRUE
  AND NOT EXISTS (SELECT 1 FROM lesson_scopes ls WHERE ls.lesson_id = l.id);
--rollback SELECT 1;

--changeset k1mb1:043-lesson-scopes-finalize
ALTER TABLE lesson_scopes
    ALTER COLUMN started_at SET NOT NULL;
DROP INDEX IF EXISTS uk_lesson_scopes_active;
CREATE UNIQUE INDEX uk_lesson_scopes_active
    ON lesson_scopes (lesson_id, started_at, group_id, allowed_subgroup_id) NULLS NOT DISTINCT
    WHERE archived_at IS NULL;
CREATE INDEX idx_lesson_scopes_started_at ON lesson_scopes (started_at);
--rollback DROP INDEX IF EXISTS idx_lesson_scopes_started_at;
--rollback DROP INDEX IF EXISTS uk_lesson_scopes_active;
--rollback CREATE UNIQUE INDEX uk_lesson_scopes_active ON lesson_scopes (lesson_id, group_id, allowed_subgroup_id) NULLS NOT DISTINCT WHERE archived_at IS NULL;
--rollback ALTER TABLE lesson_scopes ALTER COLUMN started_at DROP NOT NULL;

--changeset k1mb1:044-add-order-index-to-lessons
ALTER TABLE lessons
    ADD COLUMN order_index INTEGER;
WITH numbered AS (SELECT id,
                         ROW_NUMBER()
                             OVER (PARTITION BY subject_id, lesson_type ORDER BY started_at, id) AS rn
                  FROM lessons
                  WHERE archived_at IS NULL)
UPDATE lessons l
SET order_index = numbered.rn FROM numbered
WHERE l.id = numbered.id;
UPDATE lessons
SET order_index = 0
WHERE order_index IS NULL;
ALTER TABLE lessons
    ALTER COLUMN order_index SET NOT NULL;
CREATE UNIQUE INDEX uk_lessons_subject_type_order
    ON lessons (subject_id, lesson_type, order_index) WHERE archived_at IS NULL;
--rollback DROP INDEX IF EXISTS uk_lessons_subject_type_order;
--rollback ALTER TABLE lessons DROP COLUMN order_index;

--changeset k1mb1:045-drop-lesson-date-and-all-groups
DROP INDEX IF EXISTS idx_lessons_started_at;
ALTER TABLE lessons
DROP
COLUMN started_at,
    DROP
COLUMN all_groups;
--rollback ALTER TABLE lessons ADD COLUMN started_at DATE;
--rollback ALTER TABLE lessons ADD COLUMN all_groups BOOLEAN NOT NULL DEFAULT false;
--rollback CREATE INDEX idx_lessons_started_at ON lessons (started_at);

--changeset k1mb1:046-attendances-to-lesson-scope
ALTER TABLE attendances
    ADD COLUMN lesson_scope_id UUID REFERENCES lesson_scopes (id) ON DELETE CASCADE;
UPDATE attendances a
SET lesson_scope_id = ls.id FROM lesson_scopes ls,
    students s
WHERE a.lesson_id = ls.lesson_id
  AND a.student_id = s.id
  AND ls.archived_at IS NULL
  AND (ls.all_groups = TRUE
   OR ls.group_id = s.group_id);
ALTER TABLE attendances
    ALTER COLUMN lesson_scope_id SET NOT NULL;
ALTER TABLE attendances DROP CONSTRAINT IF EXISTS uk_attendance_student_lesson;
DROP INDEX IF EXISTS idx_attendances_lesson_id;
ALTER TABLE attendances DROP COLUMN lesson_id;
ALTER TABLE attendances
    ADD CONSTRAINT uk_attendance_student_lesson_scope UNIQUE (student_id, lesson_scope_id);
CREATE INDEX idx_attendances_lesson_scope_id ON attendances (lesson_scope_id);
--rollback DROP INDEX IF EXISTS idx_attendances_lesson_scope_id;
--rollback ALTER TABLE attendances DROP CONSTRAINT IF EXISTS uk_attendance_student_lesson_scope;
--rollback ALTER TABLE attendances ADD COLUMN lesson_id UUID REFERENCES lessons (id) ON DELETE CASCADE;
--rollback CREATE INDEX idx_attendances_lesson_id ON attendances (lesson_id);
--rollback ALTER TABLE attendances ADD CONSTRAINT uk_attendance_student_lesson UNIQUE (student_id, lesson_id);
--rollback ALTER TABLE attendances DROP COLUMN lesson_scope_id;

--changeset k1mb1:047-checkin-sessions-to-lesson-scope
ALTER TABLE check_in_sessions
    ADD COLUMN lesson_scope_id UUID REFERENCES lesson_scopes (id) ON DELETE CASCADE;
UPDATE check_in_sessions s
SET lesson_scope_id = (SELECT ls.id
                       FROM lesson_scopes ls
                       WHERE ls.lesson_id = s.lesson_id
                         AND ls.archived_at IS NULL
                       ORDER BY ls.all_groups DESC, ls.created_at
    LIMIT 1);
DELETE
FROM check_in_sessions
WHERE lesson_scope_id IS NULL;
ALTER TABLE check_in_sessions
    ALTER COLUMN lesson_scope_id SET NOT NULL;
DROP INDEX IF EXISTS uk_check_in_sessions_active_per_lesson;
DROP INDEX IF EXISTS uk_check_in_sessions_lesson_open;
DROP INDEX IF EXISTS idx_check_in_sessions_lesson;
ALTER TABLE check_in_sessions DROP COLUMN lesson_id;
CREATE INDEX idx_check_in_sessions_lesson_scope ON check_in_sessions (lesson_scope_id);
CREATE UNIQUE INDEX uk_check_in_sessions_active_per_scope
    ON check_in_sessions (lesson_scope_id) WHERE confirmed_at IS NULL AND cancelled_at IS NULL;
--rollback DROP INDEX IF EXISTS uk_check_in_sessions_active_per_scope;
--rollback DROP INDEX IF EXISTS idx_check_in_sessions_lesson_scope;
--rollback ALTER TABLE check_in_sessions ADD COLUMN lesson_id UUID REFERENCES lessons (id) ON DELETE CASCADE;
--rollback CREATE INDEX idx_check_in_sessions_lesson ON check_in_sessions (lesson_id);
--rollback CREATE UNIQUE INDEX uk_check_in_sessions_active_per_lesson ON check_in_sessions (lesson_id) WHERE confirmed_at IS NULL AND cancelled_at IS NULL;
--rollback ALTER TABLE check_in_sessions DROP COLUMN lesson_scope_id;
