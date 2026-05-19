--liquibase formatted sql

--changeset k1mb1:011-create-permission-scopes
CREATE TABLE teacher_subject_permission_scopes
(
    id                  UUID PRIMARY KEY,
    permission_id       UUID        NOT NULL REFERENCES teacher_subject_permissions (id) ON DELETE CASCADE,
    group_id            UUID        NOT NULL REFERENCES groups (id) ON DELETE CASCADE,
    allowed_subgroup_id UUID REFERENCES subgroups (id) ON DELETE CASCADE,
    allowed_lesson_type lesson_type,
    archived_at         TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_permission_scopes_permission ON teacher_subject_permission_scopes (permission_id);
CREATE INDEX idx_permission_scopes_group ON teacher_subject_permission_scopes (group_id);
CREATE UNIQUE INDEX uk_permission_scopes_active
    ON teacher_subject_permission_scopes (permission_id, group_id, allowed_subgroup_id, allowed_lesson_type)
        NULLS NOT DISTINCT
    WHERE archived_at IS NULL;
--rollback DROP TABLE teacher_subject_permission_scopes;

--changeset k1mb1:012-add-scope-id-check-in-sessions
ALTER TABLE check_in_sessions ADD COLUMN scope_id UUID REFERENCES teacher_subject_permission_scopes (id) ON DELETE CASCADE;
--rollback ALTER TABLE check_in_sessions DROP COLUMN scope_id;

--changeset k1mb1:013-migrate-permissions-to-scopes
-- 1. mapping: each active old permission -> a freshly-generated scope id
CREATE TEMP TABLE perm_scope_map
(
    old_perm_id UUID PRIMARY KEY,
    scope_id    UUID NOT NULL
) ON COMMIT DROP;
INSERT INTO perm_scope_map (old_perm_id, scope_id)
SELECT id, gen_random_uuid()
FROM teacher_subject_permissions
WHERE archived_at IS NULL;

-- 2. canonical permission per (teacher_id, subject_id) - keep oldest
CREATE TEMP TABLE perm_canonical
(
    old_id       UUID PRIMARY KEY,
    canonical_id UUID NOT NULL
) ON COMMIT DROP;
INSERT INTO perm_canonical (old_id, canonical_id)
SELECT p.id,
       (SELECT p2.id FROM teacher_subject_permissions p2
         WHERE p2.teacher_id = p.teacher_id
           AND p2.subject_id = p.subject_id
           AND p2.archived_at IS NULL
         ORDER BY p2.created_at, p2.id
         LIMIT 1)
FROM teacher_subject_permissions p
WHERE p.archived_at IS NULL;

-- 3. insert scopes (attached to canonical permission)
INSERT INTO teacher_subject_permission_scopes
    (id, permission_id, group_id, allowed_subgroup_id, allowed_lesson_type, created_at, updated_at)
SELECT psm.scope_id,
       pc.canonical_id,
       p.group_id,
       p.allowed_subgroup_id,
       p.allowed_lesson_type,
       p.created_at,
       p.updated_at
FROM teacher_subject_permissions p
JOIN perm_scope_map psm ON psm.old_perm_id = p.id
JOIN perm_canonical pc ON pc.old_id = p.id;

-- 4. repoint existing check-in sessions to the new scope
UPDATE check_in_sessions s
SET scope_id = psm.scope_id
FROM perm_scope_map psm
WHERE s.permission_id = psm.old_perm_id;

-- 5. drop now-redundant non-canonical permissions (their scopes already live on canonical)
DELETE FROM teacher_subject_permissions p
USING perm_canonical pc
WHERE p.id = pc.old_id AND pc.old_id <> pc.canonical_id;

-- 6. drop now-obsolete columns / indexes on permissions
DROP INDEX IF EXISTS idx_permissions_group;
DROP INDEX IF EXISTS uk_permissions_active;
DROP INDEX IF EXISTS uk_permissions_teacher_subject_active;
ALTER TABLE teacher_subject_permissions
    DROP COLUMN group_id,
    DROP COLUMN allowed_subgroup_id,
    DROP COLUMN allowed_lesson_type;
CREATE UNIQUE INDEX uk_permissions_teacher_subject_active
    ON teacher_subject_permissions (teacher_id, subject_id) WHERE archived_at IS NULL;
--rollback SELECT 1; -- non-reversible data migration

--changeset k1mb1:014-finalize-check-in-sessions-scope
ALTER TABLE check_in_sessions ALTER COLUMN scope_id SET NOT NULL;
ALTER TABLE check_in_sessions DROP COLUMN permission_id;
DROP INDEX IF EXISTS idx_check_in_sessions_permission;
CREATE INDEX IF NOT EXISTS idx_check_in_sessions_scope ON check_in_sessions (scope_id);
--rollback SELECT 1; -- non-reversible
