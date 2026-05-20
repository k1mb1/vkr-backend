--liquibase formatted sql

--changeset k1mb1:009-create-assignments
CREATE TABLE assignments
(
    id        UUID         PRIMARY KEY,
    lesson_id UUID         NOT NULL REFERENCES lessons (id) ON DELETE CASCADE,
    title     VARCHAR(255) NOT NULL,
    mandatory BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_assignments_lesson_id ON assignments (lesson_id);
--rollback DROP TABLE assignments;

--changeset k1mb1:010-create-grades
CREATE TABLE grades
(
    id            UUID             PRIMARY KEY,
    student_id    UUID             NOT NULL REFERENCES students (id)    ON DELETE CASCADE,
    assignment_id UUID             NOT NULL REFERENCES assignments (id) ON DELETE CASCADE,
    value         DOUBLE PRECISION NOT NULL CHECK (value > 0),
    comment       TEXT,
    created_at    TIMESTAMPTZ      NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ      NOT NULL DEFAULT now(),
    CONSTRAINT uk_grade_student_assignment UNIQUE (student_id, assignment_id)
);
CREATE INDEX idx_grades_assignment_id ON grades (assignment_id);
CREATE INDEX idx_grades_student_id    ON grades (student_id);
--rollback DROP TABLE grades;
