--liquibase formatted sql

--changeset k1mb1:018-scope-lesson-type splitStatements:true endDelimiter:;
ALTER TABLE teacher_subject_permission_scopes
    ADD COLUMN allowed_lesson_type lesson_type;

UPDATE teacher_subject_permission_scopes s
SET allowed_lesson_type = (SELECT p.allowed_lesson_type
                           FROM teacher_subject_permissions p
                           WHERE p.id = s.permission_id)
WHERE s.archived_at IS NULL;

ALTER TABLE teacher_subject_permissions
DROP
COLUMN allowed_lesson_type;
--rollback ALTER TABLE teacher_subject_permissions ADD COLUMN allowed_lesson_type lesson_type;
--rollback UPDATE teacher_subject_permissions p SET allowed_lesson_type = NULL;
--rollback ALTER TABLE teacher_subject_permission_scopes DROP COLUMN allowed_lesson_type;
