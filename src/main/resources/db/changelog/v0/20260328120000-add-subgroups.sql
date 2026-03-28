--liquibase formatted sql
--changeset k1mb1:20260328-add-subgroups

-- Add subgroup column to students (which subgroup the student belongs to)
-- NULL means the student is not assigned to any subgroup yet
ALTER TABLE STUDENTS ADD COLUMN SUBGROUP SMALLINT;

-- Add subgroup column to lessons (which subgroup attends this lesson)
-- NULL means the entire group attends (e.g. lectures)
ALTER TABLE LESSONS ADD COLUMN SUBGROUP SMALLINT;

-- Index for filtering lessons by subgroup
CREATE INDEX IX_LESSONS_SUBGROUP ON LESSONS (SUBGROUP);

-- Index for filtering students by subgroup
CREATE INDEX IX_STUDENTS_SUBGROUP ON STUDENTS (SUBGROUP);

--rollback DROP INDEX IF EXISTS IX_STUDENTS_SUBGROUP;
--rollback DROP INDEX IF EXISTS IX_LESSONS_SUBGROUP;
--rollback ALTER TABLE LESSONS DROP COLUMN IF EXISTS SUBGROUP;
--rollback ALTER TABLE STUDENTS DROP COLUMN IF EXISTS SUBGROUP;
