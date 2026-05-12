--liquibase formatted sql
--changeset k1mb1:20260509-entities-partial-indexes
--liquibase formatted sql

--changeset k1mb1:000-create-enums
CREATE TYPE lesson_type AS ENUM ('LECTURE', 'PRACTICE','NONE');
CREATE TYPE attendance_status AS ENUM ('PRESENT', 'ABSENT', 'LATE', 'EXCUSED','NONE');
--rollback DROP TYPE IF EXISTS attendance_status CASCADE;
--rollback DROP TYPE IF EXISTS lesson_type CASCADE;

--changeset k1mb1:001-create-groups
CREATE TABLE groups
(
    id         UUID PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL,
    updated_at TIMESTAMPTZ  NOT NULL,
    CONSTRAINT uk_groups_name UNIQUE (name)
);
--rollback DROP TABLE groups;

--changeset k1mb1:002-create-subgroups
CREATE TABLE subgroups
(
    id         UUID PRIMARY KEY,
    index      INTEGER     NOT NULL,
    group_id   UUID        NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_subgroups_group
        FOREIGN KEY (group_id) REFERENCES groups (id)
            ON DELETE CASCADE,
    CONSTRAINT uk_subgroup_group_index UNIQUE (group_id, index)
);
CREATE INDEX idx_subgroups_group_id ON subgroups (group_id);
--rollback DROP TABLE subgroups;

--changeset k1mb1:003-create-teachers
CREATE TABLE teachers
(
    id         UUID PRIMARY KEY,
    username   VARCHAR(255) NOT NULL,
    email      VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL,
    updated_at TIMESTAMPTZ  NOT NULL,

    CONSTRAINT uk_teachers_email UNIQUE (email)
);
--rollback DROP TABLE teachers;

--changeset k1mb1:004-create-students
CREATE TABLE students
(
    id          UUID PRIMARY KEY,
    username    VARCHAR(255) NOT NULL,
    group_id    UUID         NOT NULL,
    subgroup_id UUID,
    archived_at TIMESTAMPTZ,
    created_at  TIMESTAMPTZ  NOT NULL,
    updated_at  TIMESTAMPTZ  NOT NULL,
    CONSTRAINT fk_students_group
        FOREIGN KEY (group_id) REFERENCES groups (id),
    CONSTRAINT fk_students_subgroup
        FOREIGN KEY (subgroup_id) REFERENCES subgroups (id)
);
CREATE INDEX idx_students_group_id ON students (group_id);
CREATE INDEX idx_students_subgroup_id ON students (subgroup_id);

--rollback DROP TABLE students;

--changeset k1mb1:005-create-subjects
CREATE TABLE subjects
(
    id          UUID PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    archived_at TIMESTAMPTZ,
    created_at  TIMESTAMPTZ  NOT NULL,
    updated_at  TIMESTAMPTZ  NOT NULL
);

-- partial unique: одно и то же имя нельзя у активных, но можно после архивации
CREATE UNIQUE INDEX uk_subjects_name_active
    ON subjects (name) WHERE archived_at IS NULL;
--rollback DROP TABLE subjects;

--changeset k1mb1:006-create-subject-offerings
CREATE TABLE subject_offerings
(
    id         UUID PRIMARY KEY,
    subject_id UUID        NOT NULL,
    group_id   UUID        NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_offerings_subject
        FOREIGN KEY (subject_id) REFERENCES subjects (id),
    CONSTRAINT fk_offerings_group
        FOREIGN KEY (group_id) REFERENCES groups (id),
    CONSTRAINT uk_offering_subject_group UNIQUE (subject_id, group_id)
);
CREATE INDEX idx_offerings_subject_id ON subject_offerings (subject_id);
CREATE INDEX idx_offerings_group_id ON subject_offerings (group_id);
--rollback DROP TABLE subject_offerings;

--changeset k1mb1:007-create-subject-assignments
CREATE TABLE subject_assignments
(
    id                UUID PRIMARY KEY,
    teacher_id        UUID        NOT NULL,
    offering_id       UUID        NOT NULL,
    subgroup_id       UUID,
    lesson_type_scope lesson_type,
    created_at        TIMESTAMPTZ NOT NULL,
    updated_at        TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_assignments_teacher
        FOREIGN KEY (teacher_id) REFERENCES teachers (id),
    CONSTRAINT fk_assignments_offering
        FOREIGN KEY (offering_id) REFERENCES subject_offerings (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_assignments_subgroup
        FOREIGN KEY (subgroup_id) REFERENCES subgroups (id)
);
CREATE INDEX idx_assignments_teacher_id ON subject_assignments (teacher_id);
CREATE INDEX idx_assignments_offering_id ON subject_assignments (offering_id);

-- Уникальность с учётом того, что NULL != NULL в Postgres.
-- Случай 1: subgroup задана — обычная уникальность тройки.
CREATE UNIQUE INDEX uk_assignment_with_subgroup
    ON subject_assignments (teacher_id, offering_id, subgroup_id) WHERE subgroup_id IS NOT NULL;

-- Случай 2: subgroup = NULL (учитель ведёт offering целиком).
-- Гарантируем, что такая запись не более одной на (teacher, offering).
CREATE UNIQUE INDEX uk_assignment_full_offering
    ON subject_assignments (teacher_id, offering_id) WHERE subgroup_id IS NULL;
--rollback DROP TABLE subject_assignments;

--changeset k1mb1:008-create-lessons
CREATE TABLE lessons
(
    id          UUID PRIMARY KEY,
    offering_id UUID        NOT NULL,
    subgroup_id UUID,
    teacher_id  UUID,
    lesson_type lesson_type NOT NULL,
    started_at  TIMESTAMPTZ NOT NULL,
    ended_at    TIMESTAMPTZ,
    topic       TEXT,
    created_at  TIMESTAMPTZ NOT NULL,
    updated_at  TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_lessons_offering
        FOREIGN KEY (offering_id) REFERENCES subject_offerings (id),
    CONSTRAINT fk_lessons_subgroup
        FOREIGN KEY (subgroup_id) REFERENCES subgroups (id),
    CONSTRAINT fk_lessons_teacher
        FOREIGN KEY (teacher_id) REFERENCES teachers (id)
);
CREATE INDEX idx_lessons_offering_id ON lessons (offering_id);
CREATE INDEX idx_lessons_started_at ON lessons (started_at);
CREATE INDEX idx_lessons_teacher_id ON lessons (teacher_id);
--rollback DROP TABLE lessons;

--changeset k1mb1:009-create-attendances
CREATE TABLE attendances
(
    id         UUID PRIMARY KEY,
    student_id UUID              NOT NULL,
    lesson_id  UUID              NOT NULL,
    status     attendance_status NOT NULL,
    comment    TEXT,
    created_at TIMESTAMPTZ       NOT NULL,
    updated_at TIMESTAMPTZ       NOT NULL,
    CONSTRAINT fk_attendances_student
        FOREIGN KEY (student_id) REFERENCES students (id),
    CONSTRAINT fk_attendances_lesson
        FOREIGN KEY (lesson_id) REFERENCES lessons (id)
            ON DELETE CASCADE,
    CONSTRAINT uk_attendance_student_lesson UNIQUE (student_id, lesson_id)
);
CREATE INDEX idx_attendances_lesson_id ON attendances (lesson_id);
CREATE INDEX idx_attendances_student_id ON attendances (student_id);
--rollback DROP TABLE attendances;
