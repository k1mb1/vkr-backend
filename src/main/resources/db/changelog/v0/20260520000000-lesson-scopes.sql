--liquibase formatted sql

--changeset k1mb1:019-create-lesson-scopes
CREATE TABLE lesson_scopes
(
    id                  UUID PRIMARY KEY,
    lesson_id           UUID        NOT NULL REFERENCES lessons (id) ON DELETE CASCADE,
    group_id            UUID        NOT NULL REFERENCES groups (id) ON DELETE CASCADE,
    allowed_subgroup_id UUID REFERENCES subgroups (id) ON DELETE CASCADE,
    archived_at         TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_lesson_scopes_lesson ON lesson_scopes (lesson_id);
CREATE INDEX idx_lesson_scopes_group ON lesson_scopes (group_id);
CREATE UNIQUE INDEX uk_lesson_scopes_active
    ON lesson_scopes (lesson_id, group_id, allowed_subgroup_id)
        NULLS NOT DISTINCT
    WHERE archived_at IS NULL;
--rollback DROP TABLE lesson_scopes;

--changeset k1mb1:020-add-all-groups-to-lessons
ALTER TABLE lessons
    ADD COLUMN all_groups BOOLEAN NOT NULL DEFAULT false;
--rollback ALTER TABLE lessons DROP COLUMN all_groups;

--changeset k1mb1:021-migrate-lessons-to-lesson-scopes
INSERT INTO lesson_scopes (id, lesson_id, group_id, allowed_subgroup_id, created_at, updated_at)
SELECT gen_random_uuid(),
       l.id,
       l.group_id,
       l.subgroup_id,
       l.created_at,
       l.updated_at
FROM lessons l
WHERE l.archived_at IS NULL
  AND l.group_id IS NOT NULL;
--rollback SELECT 1; -- non-reversible data migration

--changeset k1mb1:022-add-lesson-scope-id-to-check-in-sessions
ALTER TABLE check_in_sessions
    ADD COLUMN lesson_scope_id UUID REFERENCES lesson_scopes (id) ON DELETE CASCADE;
--rollback ALTER TABLE check_in_sessions DROP COLUMN lesson_scope_id;

--changeset k1mb1:023-migrate-check-in-sessions-scope
UPDATE check_in_sessions s
SET lesson_scope_id = ls.id
FROM lesson_scopes ls,
     teacher_subject_permission_scopes ps
WHERE s.scope_id = ps.id
  AND ls.lesson_id = s.lesson_id
  AND ls.group_id = ps.group_id
  AND ls.allowed_subgroup_id IS NOT DISTINCT FROM ps.allowed_subgroup_id
  AND ls.archived_at IS NULL;
--rollback SELECT 1; -- non-reversible data migration

--changeset k1mb1:024-finalize-check-in-sessions-lesson-scope
ALTER TABLE check_in_sessions ALTER COLUMN lesson_scope_id SET NOT NULL;
ALTER TABLE check_in_sessions DROP COLUMN scope_id;
DROP INDEX IF EXISTS idx_check_in_sessions_scope;
CREATE INDEX IF NOT EXISTS idx_check_in_sessions_lesson_scope ON check_in_sessions (lesson_scope_id);
--rollback SELECT 1; -- non-reversible

--changeset k1mb1:025-drop-lesson-group-columns
DROP INDEX IF EXISTS idx_lessons_group_id;
DROP INDEX IF EXISTS idx_lessons_subgroup_id;
ALTER TABLE lessons
    DROP COLUMN group_id,
    DROP COLUMN subgroup_id;
--rollback ALTER TABLE lessons ADD COLUMN group_id UUID REFERENCES groups (id) ON DELETE CASCADE;
--rollback ALTER TABLE lessons ADD COLUMN subgroup_id UUID REFERENCES subgroups (id) ON DELETE SET NULL;
--rollback CREATE INDEX idx_lessons_group_id ON lessons (group_id);
--rollback CREATE INDEX idx_lessons_subgroup_id ON lessons (subgroup_id);
