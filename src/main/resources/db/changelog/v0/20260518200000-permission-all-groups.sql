--liquibase formatted sql

--changeset k1mb1:015-permission-all-groups-and-lesson-type
ALTER TABLE teacher_subject_permissions
    ADD COLUMN all_groups BOOLEAN NOT NULL DEFAULT false,
    ADD COLUMN allowed_lesson_type lesson_type;
--rollback ALTER TABLE teacher_subject_permissions DROP COLUMN all_groups, DROP COLUMN allowed_lesson_type;

--changeset k1mb1:016-migrate-lesson-type-from-scope-to-permission
UPDATE teacher_subject_permissions p
SET allowed_lesson_type = (SELECT s.allowed_lesson_type
                           FROM teacher_subject_permission_scopes s
                           WHERE s.permission_id = p.id
                             AND s.allowed_lesson_type IS NOT NULL
    LIMIT 1
    );
--rollback SELECT 1; -- non-reversible data migration

--changeset k1mb1:017-drop-lesson-type-from-scopes
ALTER TABLE teacher_subject_permission_scopes DROP COLUMN allowed_lesson_type;
--rollback ALTER TABLE teacher_subject_permission_scopes ADD COLUMN allowed_lesson_type lesson_type;
