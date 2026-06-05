--changeset k1mb1:061-subject-final-assessment-policy
-- Политика промежуточной аттестации (итоги): единый список банд + режим учёта посещаемости.

-- ============================================================
-- 1. Таблица политик
-- ============================================================
CREATE TABLE final_assessment_policies
(
    id                          UUID PRIMARY KEY,
    enabled                     BOOLEAN     NOT NULL DEFAULT false,
    attendance_mode             VARCHAR(16) NOT NULL DEFAULT 'COMBINED',
    attendance_requirement_mode VARCHAR(16),
    attendance_min_percent      INTEGER,
    attendance_min_count        INTEGER,
    attendance_count_present    BOOLEAN     NOT NULL DEFAULT false,
    attendance_count_late       BOOLEAN     NOT NULL DEFAULT false,
    attendance_count_absent     BOOLEAN     NOT NULL DEFAULT false,
    attendance_count_excused    BOOLEAN     NOT NULL DEFAULT false,
    created_at                  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at                  TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_final_assessment_attendance_mode
        CHECK (attendance_mode IN ('COMBINED', 'SEPARATE')),
    CONSTRAINT chk_final_assessment_attendance_req_mode
        CHECK (attendance_requirement_mode IS NULL
            OR attendance_requirement_mode IN ('PERCENT', 'COUNT')),
    CONSTRAINT chk_final_assessment_attendance_min_percent
        CHECK (attendance_min_percent IS NULL
            OR (attendance_min_percent >= 0 AND attendance_min_percent <= 100)),
    CONSTRAINT chk_final_assessment_attendance_min_count
        CHECK (attendance_min_count IS NULL OR attendance_min_count >= 0)
);

-- ============================================================
-- 2. Банды итоговой аттестации (@ElementCollection, по убыванию старшинства)
-- ============================================================
CREATE TABLE final_assessment_bands
(
    policy_id      UUID        NOT NULL REFERENCES final_assessment_policies (id) ON DELETE CASCADE,
    position       INTEGER     NOT NULL,
    label          VARCHAR(32) NOT NULL,
    min_points     INTEGER,
    required_tasks INTEGER,
    PRIMARY KEY (policy_id, position),
    CONSTRAINT chk_final_assessment_bands_min_points
        CHECK (min_points IS NULL OR min_points >= 0),
    CONSTRAINT chk_final_assessment_bands_required_tasks
        CHECK (required_tasks IS NULL OR required_tasks >= 0)
);

-- ============================================================
-- 3. Дефолтная политика на каждый существующий предмет + связка
-- ============================================================
ALTER TABLE subjects
    ADD COLUMN final_assessment_policy_id UUID;

CREATE TEMP TABLE fa_policy_migration AS
SELECT id                AS subject_id,
       gen_random_uuid() AS policy_id
FROM subjects;

INSERT INTO final_assessment_policies (id, enabled)
SELECT policy_id, false
FROM fa_policy_migration;

UPDATE subjects
SET final_assessment_policy_id = fm.policy_id
FROM fa_policy_migration fm
WHERE subjects.id = fm.subject_id;

ALTER TABLE subjects
    ALTER COLUMN final_assessment_policy_id SET NOT NULL;

ALTER TABLE subjects
    ADD CONSTRAINT fk_subjects_final_assessment_policy
        FOREIGN KEY (final_assessment_policy_id) REFERENCES final_assessment_policies (id) ON DELETE CASCADE;

--rollback ALTER TABLE subjects DROP CONSTRAINT IF EXISTS fk_subjects_final_assessment_policy;
--rollback ALTER TABLE subjects DROP COLUMN IF EXISTS final_assessment_policy_id;
--rollback DROP TABLE IF EXISTS final_assessment_bands;
--rollback DROP TABLE IF EXISTS final_assessment_policies;
