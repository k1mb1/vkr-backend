--liquibase formatted sql

--changeset k1mb1:20260424000000-1
--comment: Add NONE to PENALTY_MODE enum (default — no penalty)
ALTER TYPE PENALTY_MODE ADD VALUE IF NOT EXISTS 'NONE';

--changeset k1mb1:20260424000000-2
--comment: New enum for lesson issuance mode
CREATE TYPE ISSUANCE_MODE AS ENUM ('AUTO', 'MANUAL');

--changeset k1mb1:20260424000000-3
--comment: Remove decay_factor from lessons (replaced by per-lesson penalty config)
ALTER TABLE LESSONS DROP COLUMN DECAY_FACTOR;

--changeset k1mb1:20260424000000-4
--comment: Add issuance mode and issued_at to lessons
ALTER TABLE LESSONS
    ADD COLUMN ISSUANCE_MODE ISSUANCE_MODE NOT NULL DEFAULT 'AUTO',
    ADD COLUMN ISSUED_AT     TIMESTAMPTZ;

--changeset k1mb1:20260424000000-5
--comment: Move penalty config from lesson_tasks to lessons
ALTER TABLE LESSONS
    ADD COLUMN ISSUED_TASK_INDEX INT          NOT NULL DEFAULT 0,
    ADD COLUMN PENALTY_MODE      PENALTY_MODE NOT NULL DEFAULT 'NONE',
    ADD COLUMN PENALTY_STEP      NUMERIC(5,4) NOT NULL DEFAULT 0.25;

--changeset k1mb1:20260424000000-6
--comment: Drop penalty columns from lesson_tasks (now stored at lesson level)
ALTER TABLE LESSON_TASKS
    DROP COLUMN ISSUED_TASK_INDEX,
    DROP COLUMN PENALTY_MODE,
    DROP COLUMN PENALTY_STEP;
