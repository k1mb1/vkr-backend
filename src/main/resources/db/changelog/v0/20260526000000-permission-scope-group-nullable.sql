--liquibase formatted sql

--changeset k1mb1:040-permission-scope-group-nullable
-- Allow group_id IS NULL on permission scope to mean "all groups of subject".
-- Add a CHECK enforcing that allowed_subgroup_id requires group_id, since the
-- composite FK to subgroups(group_id, id) uses MATCH SIMPLE and skips when any
-- side is null.
ALTER TABLE teacher_subject_permission_scopes
    ALTER COLUMN group_id DROP NOT NULL;
ALTER TABLE teacher_subject_permission_scopes
    ADD CONSTRAINT chk_permission_scopes_subgroup_requires_group
        CHECK (allowed_subgroup_id IS NULL OR group_id IS NOT NULL);
--rollback ALTER TABLE teacher_subject_permission_scopes DROP CONSTRAINT chk_permission_scopes_subgroup_requires_group;
--rollback ALTER TABLE teacher_subject_permission_scopes ALTER COLUMN group_id SET NOT NULL;
