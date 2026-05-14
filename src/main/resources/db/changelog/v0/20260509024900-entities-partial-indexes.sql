--liquibase formatted sql

--changeset k1mb1:000-create-enums
CREATE TYPE lesson_type AS ENUM ('LECTURE', 'PRACTICE');
CREATE TYPE attendance_status AS ENUM ('PRESENT', 'ABSENT', 'LATE', 'EXCUSED');
--rollback DROP TYPE IF EXISTS attendance_status CASCADE;
--rollback DROP TYPE IF EXISTS lesson_type CASCADE;

--changeset k1mb1:001-create-groups
CREATE TABLE groups
(
    id          UUID PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    archived_at TIMESTAMPTZ,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE UNIQUE INDEX uk_groups_name_active ON groups (name) WHERE archived_at IS NULL;
--rollback DROP TABLE groups;

--changeset k1mb1:002-create-subgroups
CREATE TABLE subgroups
(
    id          UUID PRIMARY KEY,
    index       INTEGER     NOT NULL CHECK (index > 0),
    group_id    UUID        NOT NULL,
    archived_at TIMESTAMPTZ,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_subgroups_group
        FOREIGN KEY (group_id) REFERENCES groups (id) ON DELETE CASCADE
);
CREATE UNIQUE INDEX uk_subgroups_group_index_active ON subgroups (group_id, index) WHERE archived_at IS NULL;
CREATE INDEX idx_subgroups_group_id ON subgroups (group_id);
--rollback DROP TABLE subgroups;

--changeset k1mb1:003-create-teachers
CREATE TABLE teachers
(
    id          UUID PRIMARY KEY,
    username    VARCHAR(255) NOT NULL,
    email       VARCHAR(255) NOT NULL,
    archived_at TIMESTAMPTZ,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE UNIQUE INDEX uk_teachers_email_active ON teachers (email) WHERE archived_at IS NULL;
--rollback DROP TABLE teachers;

--changeset k1mb1:004-create-students
CREATE TABLE students
(
    id          UUID PRIMARY KEY,
    username    VARCHAR(255) NOT NULL,
    group_id    UUID         NOT NULL,
    subgroup_id UUID,
    archived_at TIMESTAMPTZ,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT fk_students_group
        FOREIGN KEY (group_id) REFERENCES groups (id) ON DELETE RESTRICT,
    CONSTRAINT fk_students_subgroup
        FOREIGN KEY (subgroup_id) REFERENCES subgroups (id) ON DELETE SET NULL
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
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE UNIQUE INDEX uk_subjects_name_active ON subjects (name) WHERE archived_at IS NULL;
--rollback DROP TABLE subjects;

-- ============================================================
-- 6. Права учителей (одна таблица вместо offerings+assignments)
-- ============================================================
--changeset k1mb1:006-create-teacher-subject-permissions
CREATE TABLE teacher_subject_permissions
(
    id                  UUID PRIMARY KEY,
    teacher_id          UUID        NOT NULL REFERENCES teachers(id) ON DELETE CASCADE,
    subject_id          UUID        NOT NULL REFERENCES subjects(id) ON DELETE CASCADE,
    group_id            UUID        NOT NULL REFERENCES groups(id)   ON DELETE CASCADE,
    allowed_subgroup_id UUID        REFERENCES subgroups(id) ON DELETE CASCADE,
    allowed_lesson_type lesson_type,  -- NULL = все типы
    archived_at         TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Одно правило на комбинацию (учитель, предмет, группа, подгруппа, тип)
CREATE UNIQUE INDEX uk_permissions_active
    ON teacher_subject_permissions (teacher_id, subject_id, group_id, allowed_subgroup_id, allowed_lesson_type)
    WHERE archived_at IS NULL;

CREATE INDEX idx_permissions_teacher ON teacher_subject_permissions (teacher_id);
CREATE INDEX idx_permissions_subject ON teacher_subject_permissions (subject_id);
CREATE INDEX idx_permissions_group   ON teacher_subject_permissions (group_id);
--rollback DROP TABLE teacher_subject_permissions;

-- ============================================================
-- 7. Занятия (без offering_id — напрямую subject + group)
-- ============================================================
--changeset k1mb1:007-create-lessons
CREATE TABLE lessons
(
    id          UUID PRIMARY KEY,
    subject_id  UUID        NOT NULL REFERENCES subjects(id) ON DELETE CASCADE,
    group_id    UUID        NOT NULL REFERENCES groups(id)   ON DELETE CASCADE,
    subgroup_id UUID        REFERENCES subgroups(id) ON DELETE SET NULL,
    teacher_id  UUID        REFERENCES teachers(id) ON DELETE SET NULL,
    lesson_type lesson_type NOT NULL,
    started_at  TIMESTAMPTZ NOT NULL,
    ended_at    TIMESTAMPTZ,
    topic       TEXT,
    archived_at TIMESTAMPTZ,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_lesson_time CHECK (ended_at IS NULL OR ended_at > started_at)
);
CREATE INDEX idx_lessons_subject_id  ON lessons (subject_id);
CREATE INDEX idx_lessons_group_id    ON lessons (group_id);
CREATE INDEX idx_lessons_subgroup_id ON lessons (subgroup_id);
CREATE INDEX idx_lessons_teacher_id  ON lessons (teacher_id);
CREATE INDEX idx_lessons_started_at  ON lessons (started_at);
CREATE INDEX idx_lessons_active      ON lessons (archived_at) WHERE archived_at IS NULL;
--rollback DROP TABLE lessons;

-- ============================================================
-- 8. Посещаемость
-- ============================================================
--changeset k1mb1:008-create-attendances
CREATE TABLE attendances
(
    id         UUID PRIMARY KEY,
    student_id UUID            NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    lesson_id  UUID            NOT NULL REFERENCES lessons(id)  ON DELETE CASCADE,
    status     attendance_status NOT NULL DEFAULT 'ABSENT',
    comment    TEXT,
    created_at TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ     NOT NULL DEFAULT now(),
    CONSTRAINT uk_attendance_student_lesson UNIQUE (student_id, lesson_id)
);
CREATE INDEX idx_attendances_lesson_id  ON attendances (lesson_id);
CREATE INDEX idx_attendances_student_id ON attendances (student_id);
--rollback DROP TABLE attendances;
