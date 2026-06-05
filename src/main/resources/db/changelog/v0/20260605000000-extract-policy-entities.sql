--changeset k1mb1:060-extract-policy-entities
-- Вынос всех policy из subjects в отдельные entity-таблицы.

-- ============================================================
-- 0. Временная таблица для связи subject → policy UUID
-- ============================================================
CREATE TEMP TABLE policy_migration AS
SELECT id                                    AS subject_id,
       gen_random_uuid()                     AS penalty_policy_id,
       gen_random_uuid()                     AS attendance_policy_id,
       gen_random_uuid()                     AS checkin_policy_id,
       gen_random_uuid()                     AS grading_highlight_policy_id,
       gen_random_uuid()                     AS attendance_highlight_policy_id
FROM subjects;

-- ============================================================
-- 1. Penalty policies
-- ============================================================
CREATE TABLE penalty_policies
(
    id                         UUID PRIMARY KEY,
    enabled                    BOOLEAN       NOT NULL DEFAULT false,
    operation                  VARCHAR(16),
    step                       NUMERIC(6, 3),
    grace_period_lessons       INTEGER,
    interval_lessons           INTEGER,
    max_reductions             INTEGER,
    bonus_enabled              BOOLEAN       NOT NULL DEFAULT false,
    bonus_operation            VARCHAR(16),
    bonus_step                 NUMERIC(6, 3),
    bonus_grace_period_lessons INTEGER,
    bonus_interval_lessons     INTEGER,
    bonus_max_increases        INTEGER,
    created_at                 TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at                 TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CONSTRAINT chk_penalty_policies_operation
        CHECK (operation IS NULL OR operation IN ('SUBTRACT', 'MULTIPLY')),
    CONSTRAINT chk_penalty_policies_bonus_operation
        CHECK (bonus_operation IS NULL OR bonus_operation IN ('ADD', 'MULTIPLY'))
);

INSERT INTO penalty_policies (id, enabled, operation, step, grace_period_lessons,
                              interval_lessons, max_reductions, bonus_enabled, bonus_operation,
                              bonus_step, bonus_grace_period_lessons, bonus_interval_lessons,
                              bonus_max_increases)
SELECT pm.penalty_policy_id,
       s.penalty_enabled,
       s.penalty_operation,
       s.penalty_step,
       s.penalty_grace_period_lessons,
       s.penalty_interval_lessons,
       s.penalty_max_reductions,
       s.bonus_enabled,
       s.bonus_operation,
       s.bonus_step,
       s.bonus_grace_period_lessons,
       s.bonus_interval_lessons,
       s.bonus_max_increases
FROM policy_migration pm
         JOIN subjects s ON s.id = pm.subject_id;

-- ============================================================
-- 2. Attendance policies
-- ============================================================
CREATE TABLE attendance_policies
(
    id             UUID PRIMARY KEY,
    enabled        BOOLEAN       NOT NULL DEFAULT false,
    points_present NUMERIC(6, 3) NOT NULL DEFAULT 0,
    points_late    NUMERIC(6, 3) NOT NULL DEFAULT 0,
    points_absent  NUMERIC(6, 3) NOT NULL DEFAULT 0,
    points_excused NUMERIC(6, 3) NOT NULL DEFAULT 0,
    created_at     TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ   NOT NULL DEFAULT now()
);

INSERT INTO attendance_policies (id, enabled, points_present, points_late, points_absent, points_excused)
SELECT pm.attendance_policy_id,
       s.attendance_enabled,
       s.attendance_points_present,
       s.attendance_points_late,
       s.attendance_points_absent,
       s.attendance_points_excused
FROM policy_migration pm
         JOIN subjects s ON s.id = pm.subject_id;

-- ============================================================
-- 3. Check-in policies
-- ============================================================
CREATE TABLE checkin_policies
(
    id              UUID PRIMARY KEY,
    enabled         BOOLEAN     NOT NULL DEFAULT false,
    on_time_seconds INTEGER,
    late_seconds    INTEGER,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_checkin_policies_on_time CHECK (on_time_seconds IS NULL OR on_time_seconds > 0),
    CONSTRAINT chk_checkin_policies_late CHECK (late_seconds IS NULL OR late_seconds >= 0)
);

INSERT INTO checkin_policies (id, enabled, on_time_seconds, late_seconds)
SELECT pm.checkin_policy_id, s.checkin_enabled, s.checkin_on_time_seconds, s.checkin_late_seconds
FROM policy_migration pm
         JOIN subjects s ON s.id = pm.subject_id;

-- ============================================================
-- 4. Grading highlight policies
-- ============================================================
CREATE TABLE grading_highlight_policies
(
    id                 UUID PRIMARY KEY,
    enabled            BOOLEAN     NOT NULL DEFAULT false,
    assignment_color   VARCHAR(7)  NOT NULL DEFAULT '#B3E5FC',
    full_color         VARCHAR(7)  NOT NULL DEFAULT '#FFEB3B',
    partial_low_color  VARCHAR(7)  NOT NULL DEFAULT '#FFF9C4',
    partial_high_color VARCHAR(7)  NOT NULL DEFAULT '#FFF176',
    created_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at         TIMESTAMPTZ NOT NULL DEFAULT now()
);

INSERT INTO grading_highlight_policies (id, enabled, assignment_color, full_color, partial_low_color,
                                        partial_high_color)
SELECT pm.grading_highlight_policy_id,
       s.highlight_enabled,
       s.highlight_assignment_color,
       s.highlight_full_color,
       s.highlight_partial_low_color,
       s.highlight_partial_high_color
FROM policy_migration pm
         JOIN subjects s ON s.id = pm.subject_id;

-- ============================================================
-- 5. Attendance highlight policies
-- ============================================================
CREATE TABLE attendance_highlight_policies
(
    id            UUID PRIMARY KEY,
    enabled       BOOLEAN     NOT NULL DEFAULT false,
    present_color VARCHAR(7)  NOT NULL DEFAULT '#DCFCE7',
    late_color    VARCHAR(7)  NOT NULL DEFAULT '#FEF3C7',
    absent_color  VARCHAR(7)  NOT NULL DEFAULT '#FEE2E2',
    excused_color VARCHAR(7)  NOT NULL DEFAULT '#DBEAFE',
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

INSERT INTO attendance_highlight_policies (id, enabled, present_color, late_color, absent_color, excused_color)
SELECT pm.attendance_highlight_policy_id, false, '#DCFCE7', '#FEF3C7', '#FEE2E2', '#DBEAFE'
FROM policy_migration pm;

-- ============================================================
-- 6. Добавляем FK-колонки в subjects
-- ============================================================
ALTER TABLE subjects
    ADD COLUMN penalty_policy_id UUID,
    ADD COLUMN attendance_policy_id UUID,
    ADD COLUMN checkin_policy_id UUID,
    ADD COLUMN grading_highlight_policy_id UUID,
    ADD COLUMN attendance_highlight_policy_id UUID;

-- ============================================================
-- 7. Связываем subjects с новыми policy
-- ============================================================
UPDATE subjects
SET penalty_policy_id = pm.penalty_policy_id
FROM policy_migration pm
WHERE subjects.id = pm.subject_id;

UPDATE subjects
SET attendance_policy_id = pm.attendance_policy_id
FROM policy_migration pm
WHERE subjects.id = pm.subject_id;

UPDATE subjects
SET checkin_policy_id = pm.checkin_policy_id
FROM policy_migration pm
WHERE subjects.id = pm.subject_id;

UPDATE subjects
SET grading_highlight_policy_id = pm.grading_highlight_policy_id
FROM policy_migration pm
WHERE subjects.id = pm.subject_id;

UPDATE subjects
SET attendance_highlight_policy_id = pm.attendance_highlight_policy_id
FROM policy_migration pm
WHERE subjects.id = pm.subject_id;

-- ============================================================
-- 8. NOT NULL + FK constraints
-- ============================================================
ALTER TABLE subjects
    ALTER COLUMN penalty_policy_id SET NOT NULL,
    ALTER COLUMN attendance_policy_id SET NOT NULL,
    ALTER COLUMN checkin_policy_id SET NOT NULL,
    ALTER COLUMN grading_highlight_policy_id SET NOT NULL,
    ALTER COLUMN attendance_highlight_policy_id SET NOT NULL;

ALTER TABLE subjects
    ADD CONSTRAINT fk_subjects_penalty_policy FOREIGN KEY (penalty_policy_id) REFERENCES penalty_policies (id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_subjects_attendance_policy FOREIGN KEY (attendance_policy_id) REFERENCES attendance_policies (id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_subjects_checkin_policy FOREIGN KEY (checkin_policy_id) REFERENCES checkin_policies (id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_subjects_grading_highlight_policy FOREIGN KEY (grading_highlight_policy_id) REFERENCES grading_highlight_policies (id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_subjects_attendance_highlight_policy FOREIGN KEY (attendance_highlight_policy_id) REFERENCES attendance_highlight_policies (id) ON DELETE CASCADE;

-- ============================================================
-- 9. Удаляем старые колонки из subjects
-- ============================================================
ALTER TABLE subjects
    DROP COLUMN IF EXISTS penalty_enabled,
    DROP COLUMN IF EXISTS penalty_operation,
    DROP COLUMN IF EXISTS penalty_step,
    DROP COLUMN IF EXISTS penalty_grace_period_lessons,
    DROP COLUMN IF EXISTS penalty_interval_lessons,
    DROP COLUMN IF EXISTS penalty_max_reductions,
    DROP COLUMN IF EXISTS bonus_enabled,
    DROP COLUMN IF EXISTS bonus_operation,
    DROP COLUMN IF EXISTS bonus_step,
    DROP COLUMN IF EXISTS bonus_grace_period_lessons,
    DROP COLUMN IF EXISTS bonus_interval_lessons,
    DROP COLUMN IF EXISTS bonus_max_increases,
    DROP COLUMN IF EXISTS attendance_enabled,
    DROP COLUMN IF EXISTS attendance_points_present,
    DROP COLUMN IF EXISTS attendance_points_late,
    DROP COLUMN IF EXISTS attendance_points_absent,
    DROP COLUMN IF EXISTS attendance_points_excused,
    DROP COLUMN IF EXISTS checkin_enabled,
    DROP COLUMN IF EXISTS checkin_on_time_seconds,
    DROP COLUMN IF EXISTS checkin_late_seconds,
    DROP COLUMN IF EXISTS highlight_enabled,
    DROP COLUMN IF EXISTS highlight_assignment_color,
    DROP COLUMN IF EXISTS highlight_full_color,
    DROP COLUMN IF EXISTS highlight_partial_low_color,
    DROP COLUMN IF EXISTS highlight_partial_high_color;

--rollback DROP TABLE IF EXISTS attendance_highlight_policies;
--rollback DROP TABLE IF EXISTS grading_highlight_policies;
--rollback DROP TABLE IF EXISTS checkin_policies;
--rollback DROP TABLE IF EXISTS attendance_policies;
--rollback DROP TABLE IF EXISTS penalty_policies;
