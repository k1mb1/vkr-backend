--liquibase formatted sql
--changeset k1mb1:20260404-add-lesson-tasks

-- ============================================================
-- 1. decay_factor on LESSONS
--    Коэффициент затухания занятия [0..1].
--    Фронтенд умножает сумму баллов всех заданий занятия на это значение.
--    1.0 = нет затухания.
-- ============================================================
ALTER TABLE LESSONS
    ADD COLUMN DECAY_FACTOR NUMERIC(5, 4) NOT NULL DEFAULT 1.0;

-- ============================================================
-- 2. LESSON_TASKS — задание на занятии
--
--    Ключевые поля:
--
--    max_points        — максимальный балл за задание.
--
--    position          — порядковый номер задания внутри занятия (0-based).
--                        Используется фронтендом для отображения и для
--                        определения «последнего» задания.
--
--    issued_task_index — индекс задания, начиная с которого применяется
--                        понижение коэффициента.  Например, если
--                        issued_task_index = 2 (0-based), то задания 0 и 1
--                        выдавались ранее и могут быть понижены; задание 2
--                        является «последним актуальным».
--
--    penalty_mode      — способ понижения коэффициентов у вытесненных заданий:
--                          'SUBTRACT' — вычитать penalty_step из 1.0 для каждого
--                                       предыдущего задания:
--                                       коэф[k] = max(0, 1 - penalty_step * (issued_task_index - k))
--                                       Пример (step=0.25): 1.0 -> 0.75 -> 0.5 -> 0.25
--                          'MULTIPLY' — умножать на penalty_step для каждого шага:
--                                       коэф[k] = penalty_step ^ (issued_task_index - k)
--                                       Пример (step=0.5): 1.0 -> 0.5 -> 0.25 -> 0.125
--
--    penalty_step      — величина шага для выбранного режима.
--                        При SUBTRACT: обычно 0.25 (даёт ряд 1.0, 0.75, 0.5, 0.25).
--                        При MULTIPLY:  обычно 0.5  (даёт ряд 1.0, 0.5, 0.25, 0.125).
--
--    submitted_at      — момент сдачи задания студентом (NULL = не сдано).
--                        Хранится на уровне задания как «последняя» сдача;
--                        точечные сроки сдачи конкретного студента хранятся
--                        в STUDENT_TASK_GRADES.submitted_at.
-- ============================================================
CREATE TYPE PENALTY_MODE AS ENUM ('SUBTRACT', 'MULTIPLY');

CREATE TABLE LESSON_TASKS
(
    ID                UUID          NOT NULL PRIMARY KEY,
    LESSON_ID         UUID          NOT NULL,
    TITLE             VARCHAR(255)  NOT NULL,
    DESCRIPTION       TEXT,
    MAX_POINTS        INTEGER       NOT NULL DEFAULT 100,
    POSITION          INTEGER       NOT NULL DEFAULT 0,
    ISSUED_TASK_INDEX INTEGER       NOT NULL DEFAULT 0,
    PENALTY_MODE      PENALTY_MODE  NOT NULL DEFAULT 'SUBTRACT',
    PENALTY_STEP      NUMERIC(5, 4) NOT NULL DEFAULT 0.25,
    CREATED_AT        TIMESTAMPTZ   NOT NULL,
    UPDATED_AT        TIMESTAMPTZ   NOT NULL,
    CONSTRAINT FK_LESSON_TASKS_LESSON
        FOREIGN KEY (LESSON_ID) REFERENCES LESSONS (ID) ON DELETE CASCADE
);

CREATE INDEX IX_LESSON_TASKS_LESSON_ID ON LESSON_TASKS (LESSON_ID);

-- ============================================================
-- 3. STUDENT_TASK_GRADES — оценка студента за конкретное задание
--
--    submitted_at — когда студент сдал задание.
--                   NULL означает «задание выдано, но ещё не сдано».
-- ============================================================
CREATE TABLE STUDENT_TASK_GRADES
(
    ID           UUID        NOT NULL PRIMARY KEY,
    TASK_ID      UUID        NOT NULL,
    STUDENT_ID   UUID        NOT NULL,
    VALUE        INTEGER,
    COMMENT      VARCHAR(255),
    SUBMITTED_AT TIMESTAMPTZ,
    CREATED_AT   TIMESTAMPTZ NOT NULL,
    UPDATED_AT   TIMESTAMPTZ NOT NULL,
    CONSTRAINT FK_TASK_GRADES_TASK
        FOREIGN KEY (TASK_ID) REFERENCES LESSON_TASKS (ID) ON DELETE CASCADE,
    CONSTRAINT FK_TASK_GRADES_STUDENT
        FOREIGN KEY (STUDENT_ID) REFERENCES STUDENTS (ID) ON DELETE CASCADE,
    CONSTRAINT UK_TASK_GRADE_TASK_STUDENT UNIQUE (TASK_ID, STUDENT_ID)
);

CREATE INDEX IX_STUDENT_TASK_GRADES_TASK_ID    ON STUDENT_TASK_GRADES (TASK_ID);
CREATE INDEX IX_STUDENT_TASK_GRADES_STUDENT_ID ON STUDENT_TASK_GRADES (STUDENT_ID);

--rollback DROP INDEX IF EXISTS IX_STUDENT_TASK_GRADES_STUDENT_ID;
--rollback DROP INDEX IF EXISTS IX_STUDENT_TASK_GRADES_TASK_ID;
--rollback DROP TABLE  IF EXISTS STUDENT_TASK_GRADES;
--rollback DROP INDEX  IF EXISTS IX_LESSON_TASKS_LESSON_ID;
--rollback DROP TABLE  IF EXISTS LESSON_TASKS;
--rollback DROP TYPE   IF EXISTS PENALTY_MODE;
--rollback ALTER TABLE LESSONS DROP COLUMN IF EXISTS DECAY_FACTOR;
