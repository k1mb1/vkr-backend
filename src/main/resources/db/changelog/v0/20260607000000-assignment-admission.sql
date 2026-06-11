--liquibase formatted sql

--changeset k1mb1:062-assignment-admission

-- ============================================================
-- 1. Переделать final_assessment_bands: добавить UUID PK
-- ============================================================

-- Удаляем старый составной PK
ALTER TABLE final_assessment_bands DROP CONSTRAINT final_assessment_bands_pkey;

-- Добавляем суррогатный UUID-PK + аудит-колонки от BaseEntity
ALTER TABLE final_assessment_bands ADD COLUMN id UUID;
ALTER TABLE final_assessment_bands ADD COLUMN created_at TIMESTAMPTZ NOT NULL DEFAULT now();
ALTER TABLE final_assessment_bands ADD COLUMN updated_at TIMESTAMPTZ NOT NULL DEFAULT now();
UPDATE final_assessment_bands SET id = gen_random_uuid();
ALTER TABLE final_assessment_bands ALTER COLUMN id SET NOT NULL;
ALTER TABLE final_assessment_bands ADD PRIMARY KEY (id);

-- Уникальность позиции в рамках политики оставляем для OrderColumn
ALTER TABLE final_assessment_bands ADD CONSTRAINT uk_final_assessment_bands_policy_position UNIQUE (policy_id, position);

-- ============================================================
-- 2. Таблица tiers допуска заданий (ссылка band_id на банду)
-- ============================================================
CREATE TABLE assignment_admission_tiers
(
    assignment_id UUID    NOT NULL REFERENCES assignments (id) ON DELETE CASCADE,
    position      INTEGER NOT NULL,
    band_id       UUID    NOT NULL REFERENCES final_assessment_bands (id) ON DELETE CASCADE,
    min_score     INTEGER,
    PRIMARY KEY (assignment_id, position),
    CONSTRAINT chk_assignment_admission_tiers_min_score CHECK (min_score IS NULL OR min_score >= 0)
);

-- ============================================================
-- 3. Колонки admission в assignments
-- ============================================================
ALTER TABLE assignments
    ADD COLUMN admission_mode VARCHAR(16) DEFAULT 'NONE',
    ADD COLUMN admission_min_score INTEGER,
    ADD CONSTRAINT chk_assignments_admission_mode CHECK (admission_mode IN ('NONE', 'PASS_FAIL', 'MIN_SCORE', 'TIERED')),
    ADD CONSTRAINT chk_assignments_admission_min_score CHECK (admission_min_score IS NULL OR admission_min_score >= 0);

--rollback DROP TABLE IF EXISTS assignment_admission_tiers;
--rollback ALTER TABLE assignments DROP COLUMN IF EXISTS admission_mode, DROP COLUMN IF EXISTS admission_min_score;
--rollback ALTER TABLE final_assessment_bands DROP CONSTRAINT IF EXISTS uk_final_assessment_bands_policy_position;
--rollback ALTER TABLE final_assessment_bands DROP CONSTRAINT IF EXISTS final_assessment_bands_pkey;
--rollback ALTER TABLE final_assessment_bands DROP COLUMN IF EXISTS id;
--rollback ALTER TABLE final_assessment_bands DROP COLUMN IF EXISTS created_at;
--rollback ALTER TABLE final_assessment_bands DROP COLUMN IF EXISTS updated_at;
--rollback ALTER TABLE final_assessment_bands ADD PRIMARY KEY (policy_id, position);
