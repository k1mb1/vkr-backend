--liquibase formatted sql

--changeset k1mb1:026-rename-all-groups-to-all-permissions
ALTER TABLE teacher_subject_permissions RENAME COLUMN all_groups TO all_permissions;
--rollback ALTER TABLE teacher_subject_permissions RENAME COLUMN all_permissions TO all_groups;

--changeset k1mb1:027-create-subject-groups
CREATE TABLE subject_groups
(
    subject_id UUID        NOT NULL REFERENCES subjects (id) ON DELETE CASCADE,
    group_id   UUID        NOT NULL REFERENCES groups (id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (subject_id, group_id)
);
CREATE INDEX idx_subject_groups_group ON subject_groups (group_id);
--rollback DROP TABLE subject_groups;

--changeset k1mb1:028-backfill-subject-groups
INSERT INTO subject_groups (subject_id, group_id)
SELECT DISTINCT p.subject_id, ps.group_id
FROM teacher_subject_permissions p
         JOIN teacher_subject_permission_scopes ps ON ps.permission_id = p.id
WHERE p.archived_at IS NULL
  AND ps.archived_at IS NULL ON CONFLICT DO NOTHING;
--rollback SELECT 1; -- non-reversible data migration

--changeset k1mb1:029-archive-scopes-for-all-permissions
UPDATE teacher_subject_permission_scopes s
SET archived_at = now() FROM teacher_subject_permissions p
WHERE s.permission_id = p.id
  AND p.all_permissions = true
  AND s.archived_at IS NULL;
--rollback SELECT 1; -- non-reversible data migration
