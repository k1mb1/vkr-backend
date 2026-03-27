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

interface LessonScheduleEntry {
  type: LessonType;           // обязательно, LECTURE или PRACTICE
  recurrence: RecurrenceType; // обязательно, WEEKLY или MONTHLY
  daysOfWeek: DayOfWeek[];    // обязательно, минимум 1 день
  time: string;               // обязательно, формат "HH:mm" (UTC), например "09:00"
  startDate: string;          // обязательно, формат "YYYY-MM-DD"
  intervalWeeks?: number;     // обязательно если recurrence == WEEKLY, минимум 1
  intervalMonths?: number;    // обязательно если recurrence == MONTHLY, минимум 1
  totalCount: number;         // обязательно, минимум 1 — сколько занятий сгенерировать
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
  id: string;          // UUID
  name: string;        // "Лекция 1", "Практика 3 (2)" и т.д.
  dateTime: string;    // ISO 8601 с timezone, например "2025-09-01T09:00:00Z"
  type: LessonType;
  subjectId: string;
  archived: boolean;
  archivedAt: string | null;
  createdAt: string;
  updatedAt: string;
}

type BulkScheduleResponse = LessonResponse[];
```

---

## Логика интерфейса

### Структура страницы

```
[Заголовок "Создание занятий"]

[+ Добавить блок лекций] [+ Добавить блок практик]

--- Блок 1: ЛЕКЦИИ ---
  Тип: [ЛЕКЦИЯ]  Повторение: [каждую неделю ▼]  каждые [1] нед.
  Дни: [ПН] [ВТ] [СР] [ЧТ] [ПТ] [СБ] [ВС]   (кнопки-переключатели)
  Время: [09:00]
  Начало: [01.09.2025]  Количество: [16]
  [Удалить блок]

--- Блок 2: ПРАКТИКИ ---
  Тип: [ПРАКТИКА]  Повторение: [каждые N недель ▼]  каждые [2] нед.
  Дни: [СР] [ПТ]
  Время: [11:00]
  Начало: [03.09.2025]  Количество: [10]
  [Удалить блок]

[Предпросмотр: 26 занятий будет создано]

[Создать занятия]
```

---

### Поля блока

| Поле | UI-элемент | Значения | Обязательно |
|---|---|---|---|
| `type` | Скрыт / читается из контекста блока | `LECTURE`, `PRACTICE` | да |
| `recurrence` | Select | "Каждую неделю" → `WEEKLY`, "Каждый месяц" → `MONTHLY` | да |
| `intervalWeeks` | Number input | min 1, показывается только если `WEEKLY` | если WEEKLY |
| `intervalMonths` | Number input | min 1, показывается только если `MONTHLY` | если MONTHLY |
| `daysOfWeek` | Toggle-кнопки (Пн, Вт, Ср, Чт, Пт, Сб, Вс) | мультиселект | да, мин. 1 |
| `time` | Time picker | "HH:mm" | да |
| `startDate` | Date picker | "YYYY-MM-DD" | да |
| `totalCount` | Number input | min 1 | да |

---

### Правила отображения

1. **Интервал:** если `recurrence = WEEKLY` — показывать `intervalWeeks`, прятать `intervalMonths`. И наоборот.
2. **Два занятия в один день:** пользователь просто добавляет два блока одного типа с одинаковыми днями, но разным `time`. Показать предупреждение:
   > "Два блока одного типа пересекаются по дням — занятия получат суффикс (1), (2) в названии"
3. **Предпросмотр счётчика:** вычислять на клиенте как `schedules.reduce((sum, s) => sum + s.totalCount, 0)`.
4. **Валидация перед отправкой:**
   - Минимум 1 блок
   - Каждый блок: выбран тип, выбраны дни, задано время, задана дата начала, totalCount >= 1
   - Если WEEKLY — intervalWeeks >= 1
   - Если MONTHLY — intervalMonths >= 1

---

### Конструирование запроса

```ts
function buildRequest(subjectId: string, blocks: FormBlock[]): BulkScheduleLessonsRequest {
  return {
    subjectId,
    schedules: blocks.map(block => ({
      type: block.type,                     // "LECTURE" | "PRACTICE"
      recurrence: block.recurrence,         // "WEEKLY" | "MONTHLY"
      daysOfWeek: block.selectedDays,       // ["MONDAY", "WEDNESDAY"]
      time: block.time,                     // "09:00"
      startDate: block.startDate,           // "2025-09-01"
      ...(block.recurrence === "WEEKLY"
        ? { intervalWeeks: block.interval }
        : { intervalMonths: block.interval }
      ),
      totalCount: block.totalCount,
    })),
  };
}
```

---

### Обработка ответа

- `201` — показать тост "Создано N занятий", обновить список занятий предмета.
- `400` — показать текст ошибки из `message` поля ответа под соответствующим полем формы.
- `404` — "Предмет не найден".

---

## Пример итогового запроса

```json
{
  "subjectId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "schedules": [
    {
      "type": "LECTURE",
      "recurrence": "WEEKLY",
      "daysOfWeek": ["MONDAY"],
      "time": "09:00",
      "startDate": "2025-09-01",
      "intervalWeeks": 1,
      "totalCount": 16
    },
    {
      "type": "PRACTICE",
      "recurrence": "WEEKLY",
      "daysOfWeek": ["WEDNESDAY", "FRIDAY"],
      "time": "11:00",
      "startDate": "2025-09-03",
      "intervalWeeks": 2,
      "totalCount": 10
    }
  ]
}
```

---

## Маппинг дней для отображения

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
