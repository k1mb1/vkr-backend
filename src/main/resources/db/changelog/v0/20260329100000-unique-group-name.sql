-- Unique index: group name must be unique within the same parent (or among root groups).
-- This replaces the application-level SELECT for uniqueness check — constraint violation
-- is caught in the service and converted to 409 Conflict.
CREATE UNIQUE INDEX IF NOT EXISTS uq_student_groups_name_parent
    ON STUDENT_GROUPS (NAME, PARENT_GROUP_ID);

-- For root groups (parent IS NULL) PostgreSQL does not include NULLs in a regular unique index,
-- so we add a partial index to enforce uniqueness among root groups separately.
CREATE UNIQUE INDEX IF NOT EXISTS uq_student_groups_name_root
    ON STUDENT_GROUPS (NAME)
    WHERE PARENT_GROUP_ID IS NULL;
