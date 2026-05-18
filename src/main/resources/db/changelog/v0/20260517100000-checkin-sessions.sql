--liquibase formatted sql

--changeset k1mb1:020-create-checkin-enum
CREATE TYPE check_in_record_status AS ENUM ('PRESENT', 'LATE');
--rollback DROP TYPE IF EXISTS check_in_record_status CASCADE;

--changeset k1mb1:021-create-check-in-sessions
CREATE TABLE check_in_sessions
(
    id              UUID PRIMARY KEY,
    lesson_id       UUID        NOT NULL REFERENCES lessons (id) ON DELETE CASCADE,
    permission_id   UUID        NOT NULL REFERENCES teacher_subject_permissions (id) ON DELETE CASCADE,
    started_at      TIMESTAMPTZ NOT NULL,
    on_time_seconds INTEGER     NOT NULL CHECK (on_time_seconds > 0),
    late_seconds    INTEGER     NOT NULL CHECK (late_seconds >= 0),
    confirmed_at    TIMESTAMPTZ,
    cancelled_at    TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_check_in_sessions_lesson ON check_in_sessions (lesson_id);
CREATE INDEX idx_check_in_sessions_permission ON check_in_sessions (permission_id);
-- only one open (unconfirmed and not cancelled) session per lesson
CREATE UNIQUE INDEX uk_check_in_sessions_lesson_open
    ON check_in_sessions (lesson_id) WHERE confirmed_at IS NULL AND cancelled_at IS NULL;
--rollback DROP TABLE check_in_sessions;

--changeset k1mb1:022-create-check-in-records
CREATE TABLE check_in_records
(
    id            UUID PRIMARY KEY,
    session_id    UUID                   NOT NULL REFERENCES check_in_sessions (id) ON DELETE CASCADE,
    student_id    UUID                   NOT NULL REFERENCES students (id) ON DELETE CASCADE,
    status        check_in_record_status NOT NULL,
    checked_in_at TIMESTAMPTZ            NOT NULL,
    created_at    TIMESTAMPTZ            NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ            NOT NULL DEFAULT now(),
    CONSTRAINT uk_check_in_records_session_student UNIQUE (session_id, student_id)
);
CREATE INDEX idx_check_in_records_session ON check_in_records (session_id);
CREATE INDEX idx_check_in_records_student ON check_in_records (student_id);
--rollback DROP TABLE check_in_records;
