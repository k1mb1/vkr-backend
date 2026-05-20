--liquibase formatted sql

--changeset k1mb1:031-grading-assignments
CREATE TABLE assignments
(
    id         UUID PRIMARY KEY,
    lesson_id  UUID        NOT NULL REFERENCES lessons (id) ON DELETE CASCADE,
    "order"    INTEGER     NOT NULL CHECK ("order" > 0),
    max_points INTEGER     NOT NULL CHECK (max_points > 0),
    required   BOOLEAN     NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_assignment_lesson_order UNIQUE (lesson_id, "order")
);
CREATE INDEX idx_assignments_lesson_id ON assignments (lesson_id);
--rollback DROP TABLE assignments;

--changeset k1mb1:031-grading-grades
CREATE TABLE grades
(
    id            UUID PRIMARY KEY,
    student_id    UUID        NOT NULL REFERENCES students (id) ON DELETE CASCADE,
    lesson_id     UUID        NOT NULL REFERENCES lessons (id) ON DELETE CASCADE,
    assignment_id UUID REFERENCES assignments (id) ON DELETE CASCADE,
    score         INTEGER     NOT NULL CHECK (score > 0),
    comment       TEXT,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_grades_lesson_id ON grades (lesson_id);
CREATE INDEX idx_grades_student_id ON grades (student_id);
CREATE INDEX idx_grades_assignment_id ON grades (assignment_id);
-- Одна "extra" оценка на пару (student, lesson) — только когда assignment не задан
CREATE UNIQUE INDEX uk_grades_student_lesson_extra
    ON grades (student_id, lesson_id) WHERE assignment_id IS NULL;
-- Одна оценка на пару (student, assignment) — когда оценка привязана к заданию
CREATE UNIQUE INDEX uk_grades_student_assignment
    ON grades (student_id, assignment_id) WHERE assignment_id IS NOT NULL;
--rollback DROP TABLE grades;
