--liquibase formatted sql

--changeset k1mb1:050-citext-teachers-email
CREATE
EXTENSION IF NOT EXISTS citext;
ALTER TABLE teachers ALTER COLUMN email TYPE citext;
--rollback ALTER TABLE teachers ALTER COLUMN email TYPE varchar(255);

--changeset k1mb1:051-check-in-sessions-confirmed-cancelled-mutex
ALTER TABLE check_in_sessions
    ADD CONSTRAINT chk_check_in_sessions_confirmed_xor_cancelled
        CHECK (confirmed_at IS NULL OR cancelled_at IS NULL);
--rollback ALTER TABLE check_in_sessions DROP CONSTRAINT chk_check_in_sessions_confirmed_xor_cancelled;

--changeset k1mb1:052-subgroups-group-fk-restrict
ALTER TABLE subgroups DROP CONSTRAINT fk_subgroups_group;
ALTER TABLE subgroups
    ADD CONSTRAINT fk_subgroups_group
        FOREIGN KEY (group_id) REFERENCES groups (id) ON DELETE RESTRICT;
--rollback ALTER TABLE subgroups DROP CONSTRAINT fk_subgroups_group;
--rollback ALTER TABLE subgroups ADD CONSTRAINT fk_subgroups_group FOREIGN KEY (group_id) REFERENCES groups (id) ON DELETE CASCADE;

--changeset k1mb1:053-subgroups-id-group-unique
ALTER TABLE subgroups
    ADD CONSTRAINT uk_subgroups_id_group UNIQUE (id, group_id);
--rollback ALTER TABLE subgroups DROP CONSTRAINT uk_subgroups_id_group;

--changeset k1mb1:054-students-subgroup-group-consistency
-- Cleanup: clear subgroup_id where it refers to a subgroup of a different group
UPDATE students s
SET subgroup_id = NULL
WHERE subgroup_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM subgroups sg WHERE sg.id = s.subgroup_id AND sg.group_id = s.group_id);
ALTER TABLE students DROP CONSTRAINT fk_students_subgroup;
ALTER TABLE students
    ADD CONSTRAINT fk_students_group_subgroup
        FOREIGN KEY (group_id, subgroup_id) REFERENCES subgroups (group_id, id) ON DELETE SET NULL;
--rollback ALTER TABLE students DROP CONSTRAINT fk_students_group_subgroup;
--rollback ALTER TABLE students ADD CONSTRAINT fk_students_subgroup FOREIGN KEY (subgroup_id) REFERENCES subgroups (id) ON DELETE SET NULL;

--changeset k1mb1:055-permission-scopes-subgroup-group-consistency
-- Cleanup: clear allowed_subgroup_id where it refers to a subgroup of a different group
UPDATE teacher_subject_permission_scopes sc
SET allowed_subgroup_id = NULL
WHERE allowed_subgroup_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM subgroups sg WHERE sg.id = sc.allowed_subgroup_id AND sg.group_id = sc.group_id);
ALTER TABLE teacher_subject_permission_scopes
    ADD CONSTRAINT fk_permission_scopes_group_subgroup
        FOREIGN KEY (group_id, allowed_subgroup_id) REFERENCES subgroups (group_id, id) ON DELETE CASCADE;
--rollback ALTER TABLE teacher_subject_permission_scopes DROP CONSTRAINT fk_permission_scopes_group_subgroup;

--changeset k1mb1:056-lesson-scopes-subgroup-group-consistency
-- Cleanup: clear allowed_subgroup_id where group_id is set and subgroup belongs to a different group
UPDATE lesson_scopes ls
SET allowed_subgroup_id = NULL
WHERE allowed_subgroup_id IS NOT NULL
  AND group_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM subgroups sg WHERE sg.id = ls.allowed_subgroup_id AND sg.group_id = ls.group_id);
-- Subgroup only makes sense when a concrete group is set
ALTER TABLE lesson_scopes
    ADD CONSTRAINT chk_lesson_scopes_subgroup_requires_group
        CHECK (allowed_subgroup_id IS NULL OR group_id IS NOT NULL);
ALTER TABLE lesson_scopes
    ADD CONSTRAINT fk_lesson_scopes_group_subgroup
        FOREIGN KEY (group_id, allowed_subgroup_id) REFERENCES subgroups (group_id, id) ON DELETE CASCADE;
--rollback ALTER TABLE lesson_scopes DROP CONSTRAINT fk_lesson_scopes_group_subgroup;
--rollback ALTER TABLE lesson_scopes DROP CONSTRAINT chk_lesson_scopes_subgroup_requires_group;

--changeset k1mb1:057-attendances-add-archived-at
ALTER TABLE attendances
    ADD COLUMN archived_at TIMESTAMPTZ;
--rollback ALTER TABLE attendances DROP COLUMN archived_at;

--changeset k1mb1:058-check-in-records-add-archived-at
ALTER TABLE check_in_records
    ADD COLUMN archived_at TIMESTAMPTZ;
--rollback ALTER TABLE check_in_records DROP COLUMN archived_at;

--changeset k1mb1:059-touch-updated-at-trigger splitStatements:false
CREATE
OR REPLACE FUNCTION touch_updated_at() RETURNS trigger AS
$$
BEGIN
    NEW.updated_at
= now();
RETURN NEW;
END;
$$
LANGUAGE plpgsql;
--rollback DROP FUNCTION IF EXISTS touch_updated_at();

--changeset k1mb1:060-attach-updated-at-triggers
CREATE TRIGGER trg_groups_updated_at
    BEFORE UPDATE
    ON groups
    FOR EACH ROW EXECUTE FUNCTION touch_updated_at();
CREATE TRIGGER trg_subgroups_updated_at
    BEFORE UPDATE
    ON subgroups
    FOR EACH ROW EXECUTE FUNCTION touch_updated_at();
CREATE TRIGGER trg_teachers_updated_at
    BEFORE UPDATE
    ON teachers
    FOR EACH ROW EXECUTE FUNCTION touch_updated_at();
CREATE TRIGGER trg_students_updated_at
    BEFORE UPDATE
    ON students
    FOR EACH ROW EXECUTE FUNCTION touch_updated_at();
CREATE TRIGGER trg_subjects_updated_at
    BEFORE UPDATE
    ON subjects
    FOR EACH ROW EXECUTE FUNCTION touch_updated_at();
CREATE TRIGGER trg_teacher_subject_permissions_updated_at
    BEFORE UPDATE
    ON teacher_subject_permissions
    FOR EACH ROW EXECUTE FUNCTION touch_updated_at();
CREATE TRIGGER trg_teacher_subject_permission_scopes_updated_at
    BEFORE UPDATE
    ON teacher_subject_permission_scopes
    FOR EACH ROW EXECUTE FUNCTION touch_updated_at();
CREATE TRIGGER trg_lessons_updated_at
    BEFORE UPDATE
    ON lessons
    FOR EACH ROW EXECUTE FUNCTION touch_updated_at();
CREATE TRIGGER trg_lesson_scopes_updated_at
    BEFORE UPDATE
    ON lesson_scopes
    FOR EACH ROW EXECUTE FUNCTION touch_updated_at();
CREATE TRIGGER trg_attendances_updated_at
    BEFORE UPDATE
    ON attendances
    FOR EACH ROW EXECUTE FUNCTION touch_updated_at();
CREATE TRIGGER trg_check_in_sessions_updated_at
    BEFORE UPDATE
    ON check_in_sessions
    FOR EACH ROW EXECUTE FUNCTION touch_updated_at();
CREATE TRIGGER trg_check_in_records_updated_at
    BEFORE UPDATE
    ON check_in_records
    FOR EACH ROW EXECUTE FUNCTION touch_updated_at();
CREATE TRIGGER trg_subject_groups_updated_at
    BEFORE UPDATE
    ON subject_groups
    FOR EACH ROW EXECUTE FUNCTION touch_updated_at();
CREATE TRIGGER trg_assignments_updated_at
    BEFORE UPDATE
    ON assignments
    FOR EACH ROW EXECUTE FUNCTION touch_updated_at();
CREATE TRIGGER trg_grades_updated_at
    BEFORE UPDATE
    ON grades
    FOR EACH ROW EXECUTE FUNCTION touch_updated_at();
--rollback DROP TRIGGER IF EXISTS trg_groups_updated_at ON groups;
--rollback DROP TRIGGER IF EXISTS trg_subgroups_updated_at ON subgroups;
--rollback DROP TRIGGER IF EXISTS trg_teachers_updated_at ON teachers;
--rollback DROP TRIGGER IF EXISTS trg_students_updated_at ON students;
--rollback DROP TRIGGER IF EXISTS trg_subjects_updated_at ON subjects;
--rollback DROP TRIGGER IF EXISTS trg_teacher_subject_permissions_updated_at ON teacher_subject_permissions;
--rollback DROP TRIGGER IF EXISTS trg_teacher_subject_permission_scopes_updated_at ON teacher_subject_permission_scopes;
--rollback DROP TRIGGER IF EXISTS trg_lessons_updated_at ON lessons;
--rollback DROP TRIGGER IF EXISTS trg_lesson_scopes_updated_at ON lesson_scopes;
--rollback DROP TRIGGER IF EXISTS trg_attendances_updated_at ON attendances;
--rollback DROP TRIGGER IF EXISTS trg_check_in_sessions_updated_at ON check_in_sessions;
--rollback DROP TRIGGER IF EXISTS trg_check_in_records_updated_at ON check_in_records;
--rollback DROP TRIGGER IF EXISTS trg_subject_groups_updated_at ON subject_groups;
--rollback DROP TRIGGER IF EXISTS trg_assignments_updated_at ON assignments;
--rollback DROP TRIGGER IF EXISTS trg_grades_updated_at ON grades;
