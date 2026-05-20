--liquibase formatted sql

--changeset k1mb1:011-assignments-rename-mandatory-add-max-score
ALTER TABLE assignments RENAME COLUMN mandatory TO required;
ALTER TABLE assignments ADD COLUMN max_score INTEGER NOT NULL DEFAULT 1 CHECK (max_score > 0);
ALTER TABLE assignments ALTER COLUMN max_score DROP DEFAULT;
--rollback ALTER TABLE assignments DROP COLUMN max_score; ALTER TABLE assignments RENAME COLUMN required TO mandatory;

--changeset k1mb1:012-grades-value-to-integer
ALTER TABLE grades ALTER COLUMN value TYPE INTEGER USING value::INTEGER;
--rollback ALTER TABLE grades ALTER COLUMN value TYPE DOUBLE PRECISION;
