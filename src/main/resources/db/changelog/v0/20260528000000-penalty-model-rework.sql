--liquibase formatted sql

--changeset k1mb1:051-grade-lessons-late
-- Зафиксированное при выставлении опоздание (в занятиях своего типа). Заменяет
-- расчёт на чтении: число хранится в строке, поэтому корректно при любом фильтре.
ALTER TABLE grades
    ADD COLUMN lessons_late INTEGER CHECK (lessons_late IS NULL OR lessons_late >= 0);
--rollback ALTER TABLE grades DROP COLUMN lessons_late;

--changeset k1mb1:052-lesson-active-per-type
-- Активное (текущее) занятие теперь по одному на (предмет, тип) — лекции и практики
-- независимы, подсчёт опоздания идёт по orderIndex внутри типа.
DROP INDEX IF EXISTS uk_lessons_active_per_subject;
CREATE UNIQUE INDEX uk_lessons_active_per_subject_type
    ON lessons (subject_id, lesson_type) WHERE active = true AND archived_at IS NULL;
--rollback DROP INDEX IF EXISTS uk_lessons_active_per_subject_type;
--rollback CREATE UNIQUE INDEX uk_lessons_active_per_subject ON lessons (subject_id) WHERE active = true AND archived_at IS NULL;
