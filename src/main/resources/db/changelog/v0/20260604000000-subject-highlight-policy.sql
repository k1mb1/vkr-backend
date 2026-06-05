--liquibase formatted sql

--changeset k1mb1:subject-highlight-policy
-- Цветовая подсветка таблицы оценок (опциональная фича). Раскраска ячеек — на фронте.
-- Цвета хранятся в HEX-формате вида #00C16A.
ALTER TABLE subjects
    ADD COLUMN highlight_enabled            BOOLEAN     NOT NULL DEFAULT false,
    ADD COLUMN highlight_assignment_color   VARCHAR(7)  NOT NULL DEFAULT '#B3E5FC',
    ADD COLUMN highlight_full_color         VARCHAR(7)  NOT NULL DEFAULT '#FFEB3B',
    ADD COLUMN highlight_partial_low_color  VARCHAR(7)  NOT NULL DEFAULT '#FFF9C4',
    ADD COLUMN highlight_partial_high_color VARCHAR(7)  NOT NULL DEFAULT '#FFF176';
--rollback ALTER TABLE subjects DROP COLUMN highlight_enabled, DROP COLUMN highlight_assignment_color, DROP COLUMN highlight_full_color, DROP COLUMN highlight_partial_low_color, DROP COLUMN highlight_partial_high_color;
