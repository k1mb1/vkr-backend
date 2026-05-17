--liquibase formatted sql

--changeset k1mb1:009-permission-unique-teacher-subject
DROP INDEX IF EXISTS uk_permissions_active;
CREATE UNIQUE INDEX uk_permissions_teacher_subject_active
    ON teacher_subject_permissions (teacher_id, subject_id) WHERE archived_at IS NULL;
--rollback DROP INDEX IF EXISTS uk_permissions_teacher_subject_active;
--rollback CREATE UNIQUE INDEX uk_permissions_active ON teacher_subject_permissions (teacher_id, subject_id, group_id, allowed_subgroup_id, allowed_lesson_type) WHERE archived_at IS NULL;
