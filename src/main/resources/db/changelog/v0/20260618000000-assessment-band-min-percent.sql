--changeset k1mb1:063-assessment-band-min-percent
-- Порог банды по проценту от максимально возможных баллов (отдельное условие рядом с min_points).

ALTER TABLE final_assessment_bands
    ADD COLUMN min_percent INTEGER;

ALTER TABLE final_assessment_bands
    ADD CONSTRAINT chk_final_assessment_bands_min_percent
        CHECK (min_percent IS NULL OR (min_percent >= 0 AND min_percent <= 100));

--rollback ALTER TABLE final_assessment_bands DROP CONSTRAINT IF EXISTS chk_final_assessment_bands_min_percent;
--rollback ALTER TABLE final_assessment_bands DROP COLUMN IF EXISTS min_percent;
