# Спека: форма массового создания занятий

## Эндпоинт

```
POST /api/lessons/bulk-schedule
Content-Type: application/json
```

---

## Модель запроса

```ts
type DayOfWeek = "MONDAY" | "TUESDAY" | "WEDNESDAY" | "THURSDAY" | "FRIDAY" | "SATURDAY" | "SUNDAY";
type LessonType = "LECTURE" | "PRACTICE";
type RecurrenceType = "WEEKLY" | "MONTHLY";

/**
 * Слот — конкретное занятие внутри цикла расписания.
 * weekIndex определяет, на какой неделе цикла срабатывает слот.
 */
interface LessonSlot {
  type: LessonType;           // LECTURE или PRACTICE
  weekIndex?: number;         // индекс недели в цикле (0..intervalWeeks-1), по умолчанию 0
  daysOfWeek: DayOfWeek[];    // дни недели, минимум 1
  time: string;               // формат "HH:mm", например "09:00"
  subgroup?: number | null;   // номер подгруппы (1, 2, ...) или null = вся группа
}

/**
 * Entry описывает один цикл расписания с набором слотов.
 * Все слоты внутри entry делят общий ритм повторения.
 */
interface LessonScheduleEntry {
  recurrence: RecurrenceType; // WEEKLY или MONTHLY
  startDate: string;          // дата начала, формат "YYYY-MM-DD"
  intervalWeeks?: number;     // каждые N недель (если WEEKLY), минимум 1
  intervalMonths?: number;    // каждые N месяцев (если MONTHLY), минимум 1
  slots: LessonSlot[];        // минимум 1 слот
  totalCount: number;         // общее количество занятий по всем слотам
}

interface BulkScheduleLessonsRequest {
  subjectId: string;              // UUID предмета
  schedules: LessonScheduleEntry[]; // минимум 1 элемент
}
```

---

## Модель ответа

Сервер возвращает `201 Created` с массивом созданных занятий:

```ts
interface LessonResponse {
  id: string;               // UUID
  name: string;             // "Лекция 1", "Практика 3 (2)" и т.д.
  dateTime: string;         // ISO 8601, например "2025-09-01T09:00:00Z"
  type: LessonType;
  subgroup: number | null;  // номер подгруппы или null (вся группа)
  subjectId: string;
  archived: boolean;
  archivedAt: string | null;
  createdAt: string;
  updatedAt: string;
}

type BulkScheduleResponse = LessonResponse[];
```

---

## Логика подгрупп

- **`subgroup = null`** — занятие для всей группы (лекция).
- **`subgroup = 1`** — занятие только для подгруппы 1 (практика).
- **`subgroup = 2`** — занятие только для подгруппы 2.

Студенты имеют поле `subgroup` (1, 2, или null). При отображении посещаемости/оценок фильтруйте:
- Если `lesson.subgroup == null` — показывать всех студентов предмета.
- Если `lesson.subgroup == N` — показывать только студентов с `student.subgroup == N`.

---

## Структура интерфейса

```
[Заголовок "Создание занятий"]

--- Цикл расписания ---
  Повторение: [каждые N недель ▼]  Интервал: [2] нед.
  Начало: [01.09.2025]  Всего занятий: [32]

  [+ Добавить слот]

  Слот 1: Лекция | Неделя [0] | Дни: [ПН] | Время: [09:00] | Подгруппа: [Вся группа ▼] [X]
  Слот 2: Практика | Неделя [0] | Дни: [СР] | Время: [11:00] | Подгруппа: [1 ▼] [X]
  Слот 3: Практика | Неделя [0] | Дни: [СР] | Время: [13:00] | Подгруппа: [2 ▼] [X]
  Слот 4: Практика | Неделя [1] | Дни: [ПН] [СР] | Время: [11:00] | Подгруппа: [Вся группа ▼] [X]

[Предпросмотр: 32 занятия]

[Создать занятия]
```

---

## Поля слота

| Поле | UI-элемент | Значения | Обязательно |
|---|---|---|---|
| `type` | Select | `LECTURE`, `PRACTICE` | да |
| `weekIndex` | Number input (0..intervalWeeks-1) | по умолчанию 0, прятать если intervalWeeks == 1 | нет |
| `daysOfWeek` | Toggle-кнопки Пн-Вс | мультиселект, мин. 1 | да |
| `time` | Time picker | "HH:mm" | да |
| `subgroup` | Select | "Вся группа" (null), "1", "2", ... | нет |

---

## Пример: практики по подгруппам

Две практики в один день (среда), но для разных подгрупп:

```json
{
  "subjectId": "...",
  "schedules": [
    {
      "recurrence": "WEEKLY",
      "intervalWeeks": 1,
      "startDate": "2025-09-03",
      "totalCount": 32,
      "slots": [
        { "type": "LECTURE",  "weekIndex": 0, "daysOfWeek": ["MONDAY"],    "time": "09:00", "subgroup": null },
        { "type": "PRACTICE", "weekIndex": 0, "daysOfWeek": ["WEDNESDAY"], "time": "11:00", "subgroup": 1 },
        { "type": "PRACTICE", "weekIndex": 0, "daysOfWeek": ["WEDNESDAY"], "time": "11:00", "subgroup": 2 }
      ]
    }
  ]
}
```

Результат: каждую среду создаются 2 практики — одна для подгруппы 1, другая для подгруппы 2.

---

## Пример: чередование недель

Лекции только на нечётных неделях, практики каждую неделю:

```json
{
  "subjectId": "...",
  "schedules": [
    {
      "recurrence": "WEEKLY",
      "intervalWeeks": 2,
      "startDate": "2025-09-01",
      "totalCount": 24,
      "slots": [
        { "type": "LECTURE",  "weekIndex": 0, "daysOfWeek": ["MONDAY"],    "time": "09:00" },
        { "type": "PRACTICE", "weekIndex": 0, "daysOfWeek": ["WEDNESDAY"], "time": "11:00" },
        { "type": "PRACTICE", "weekIndex": 1, "daysOfWeek": ["WEDNESDAY"], "time": "11:00" }
      ]
    }
  ]
}
```

---

## Конструирование запроса

```ts
interface FormSlot {
  type: LessonType;
  weekIndex: number;
  selectedDays: DayOfWeek[];
  time: string;
  subgroup: number | null;
}

interface FormEntry {
  recurrence: RecurrenceType;
  interval: number;
  startDate: string;
  totalCount: number;
  slots: FormSlot[];
}

function buildRequest(subjectId: string, entries: FormEntry[]): BulkScheduleLessonsRequest {
  return {
    subjectId,
    schedules: entries.map(entry => ({
      recurrence: entry.recurrence,
      startDate: entry.startDate,
      totalCount: entry.totalCount,
      ...(entry.recurrence === "WEEKLY"
        ? { intervalWeeks: entry.interval }
        : { intervalMonths: entry.interval }
      ),
      slots: entry.slots.map(slot => ({
        type: slot.type,
        weekIndex: slot.weekIndex,
        daysOfWeek: slot.selectedDays,
        time: slot.time,
        ...(slot.subgroup !== null && { subgroup: slot.subgroup }),
      })),
    })),
  };
}
```

---

## Обработка ответа

- `201` — показать тост "Создано N занятий", обновить список.
- `400` — показать ошибку валидации.
- `404` — "Предмет не найден".

---

## Маппинг дней

```ts
const DAY_LABELS: Record<DayOfWeek, string> = {
  MONDAY:    "Пн",
  TUESDAY:   "Вт",
  WEDNESDAY: "Ср",
  THURSDAY:  "Чт",
  FRIDAY:    "Пт",
  SATURDAY:  "Сб",
  SUNDAY:    "Вс",
};
```
