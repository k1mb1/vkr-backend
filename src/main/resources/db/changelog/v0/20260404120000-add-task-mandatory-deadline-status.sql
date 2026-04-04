--liquibase formatted sql
--changeset k1mb1:20260404120000-add-task-mandatory-deadline-status

-- ============================================================
-- 1. LESSON_TASKS — обязательность и дедлайн
--
--    is_mandatory — обязательное задание (TRUE) или бонусное (FALSE).
--                   Обязательные задания учитываются при подсчёте итога
--                   даже если студент не сдал; бонусные — только при сдаче.
--
--    deadline     — срок сдачи задания.  NULL = без ограничений.
--                   Используется фронтендом для выделения просроченных работ
--                   и для расчёта пени за опоздание (если нужно).
-- ============================================================
ALTER TABLE LESSON_TASKS
    ADD COLUMN IS_MANDATORY BOOLEAN     NOT NULL DEFAULT TRUE,
    ADD COLUMN DEADLINE     TIMESTAMPTZ;

-- ============================================================
-- 2. SUBMISSION_STATUS — статус сдачи задания студентом
--
--    NOT_SUBMITTED — задание выдано, студент ещё ничего не сдавал.
--    SUBMITTED     — студент сдал работу, ждёт проверки преподавателем.
--    GRADED        — преподаватель проверил и выставил балл (value != NULL).
--    RESUBMIT      — преподаватель вернул на доработку.
-- ============================================================
CREATE TYPE SUBMISSION_STATUS AS ENUM (
    'NOT_SUBMITTED',
    'SUBMITTED',
    'GRADED',
    'RESUBMIT'
);

ALTER TABLE STUDENT_TASK_GRADES
    ADD COLUMN STATUS SUBMISSION_STATUS NOT NULL DEFAULT 'NOT_SUBMITTED';

-- Индекс для быстрой выборки «все несданные/ждут проверки» по занятию
CREATE INDEX IX_STUDENT_TASK_GRADES_STATUS ON STUDENT_TASK_GRADES (STATUS);

--rollback DROP INDEX  IF EXISTS IX_STUDENT_TASK_GRADES_STATUS;
--rollback ALTER TABLE STUDENT_TASK_GRADES DROP COLUMN IF EXISTS STATUS;
--rollback DROP TYPE   IF EXISTS SUBMISSION_STATUS;
--rollback ALTER TABLE LESSON_TASKS DROP COLUMN IF EXISTS DEADLINE;
--rollback ALTER TABLE LESSON_TASKS DROP COLUMN IF EXISTS IS_MANDATORY;
