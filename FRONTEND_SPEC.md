# EduTrack — Спецификация фронтенда

## О приложении

**EduTrack** — веб-приложение для преподавателей вузов и колледжей. Позволяет вести учёт студентов, занятий, оценок и посещаемости по каждому учебному предмету.

**Основной сценарий:**
Преподаватель входит в систему → видит список своих предметов → открывает нужный предмет → работает с таблицами оценок и посещаемости.

---

## Стек и инфраструктура

| Параметр | Значение |
|----------|----------|
| Backend | Spring Boot, порт `8081` |
| Auth | OAuth2 JWT (Keycloak). Все запросы требуют `Authorization: Bearer <token>`, кроме Swagger |
| API docs | Swagger UI: `GET /swagger-ui/index.html`, схема: `GET /v3/api-docs` |
| CORS | Настраивается через `CORS_ALLOWED_ORIGINS`; credentials разрешены |

### Аутентификация
- Используется Keycloak (или любой OAuth2 OIDC-провайдер)
- JWT-токен содержит `sub` (UUID преподавателя)
- После логина фронт обязан вызвать `PUT /api/teachers/{id}` с данными пользователя (upsert)

---

## Сущности и структура данных

### Teacher (Преподаватель)
```typescript
{
  id: UUID          // = sub из JWT
  username: string
  email: string
  createdAt: string // ISO-8601
  updatedAt: string
}
```

### Subject (Предмет)
```typescript
{
  id: UUID
  name: string
  description: string | null
  archived: boolean
  archivedAt: string | null
  createdAt: string
  updatedAt: string
}
```

### StudentGroup (Группа студентов)
Группа может иметь подгруппы (например, "ИСТ-21/1", "ИСТ-21/2").
```typescript
// Краткий вид (списки)
{
  id: UUID
  name: string
  subgroupCount: number
}

// Полный вид (детальная страница)
{
  id: UUID
  name: string
  students: StudentEntry[]    // прямые студенты (если нет подгрупп)
  subgroups: SubgroupResponse[]
}

// Подгруппа
{
  id: UUID
  name: string         // формат: "ГруппаX/N", например "ИСТ-21/2"
  students: StudentEntry[]
}

// Краткий студент
{
  id: UUID
  username: string
}
```

### Student (Студент)
```typescript
{
  id: UUID
  username: string
  groupId: UUID | null
  createdAt: string
  updatedAt: string
}
```

### Lesson (Занятие)
```typescript
{
  id: UUID
  name: string
  dateTime: string | null     // ISO-8601 со смещением (OffsetDateTime)
  type: "LECTURE" | "PRACTICE" | "NONE"
  subjectId: UUID
  groupId: UUID | null        // null = для всей группы (лекция)
  subgroupNumber: number | null  // порядковый номер подгруппы из имени
  issuanceMode: "AUTO" | "MANUAL"
  issuedAt: string | null     // когда выдано
  issuedTaskIndex: number     // номер текущего активного задания
  penaltyMode: "NONE" | "SUBTRACT" | "MULTIPLY"
  penaltyStep: number         // 0.0001–1.0
  createdAt: string
  updatedAt: string
}
```

### LessonTask (Задание занятия)
```typescript
{
  id: UUID
  lessonId: UUID
  title: string
  description: string | null
  maxPoints: number           // >= 1
  position: number            // порядок внутри занятия (0-based)
  isMandatory: boolean        // false = бонусное
  deadline: string | null
  createdAt: string
  updatedAt: string
}
```

### StudentTaskGrade (Оценка)
```typescript
{
  id: UUID
  taskId: UUID
  lessonId: UUID
  studentId: UUID
  value: number | null        // очки; null = не оценено
  comment: string | null
  status: "NOT_SUBMITTED" | "SUBMITTED" | "GRADED" | "RESUBMIT"
  submittedAt: string | null
  createdAt: string
  updatedAt: string
}
```

### StudentAttendance (Посещаемость)
```typescript
{
  attendanceId: UUID
  lessonId: UUID
  presence: "NONE" | "PRESENT" | "NOT_PRESENT"
  note: string | null
}
```

---

## Таблица оценок предмета

`GET /api/subjects/{subjectId}/grades` возвращает:
```typescript
{
  lessons: Array<{
    lessonId: UUID
    lessonName: string
    dateTime: string | null
    type: "LECTURE" | "PRACTICE" | "NONE"
    groupId: UUID | null     // null для лекций или если группа не назначена
  }>
  students: Array<{ id: UUID, username: string }>
  grades: Array<{
    id: UUID
    taskId: UUID
    lessonId: UUID
    studentId: UUID
    value: number | null
    comment: string | null
    status: "NOT_SUBMITTED" | "SUBMITTED" | "GRADED" | "RESUBMIT"
    submittedAt: string | null
    createdAt: string
    updatedAt: string
  }>   // отсортированы по дате занятия, потом по position задания
}
```

**Query params:**
- `lessonType=LECTURE` — только лекции
- `lessonType=PRACTICE` — только практики
- `groupId=UUID` — фильтр по конкретной подгруппе

Фронт строит таблицу, матча `grades` по `studentId` (строка) и `taskId` → `lessonId` (колонка/группа колонок).

Фронт должен построить таблицу: строки = студенты, колонки = задания (сгруппированные по занятиям).

### Формула штрафа за просрочку (вычисляется на фронте)
```
d = lesson.issuedTaskIndex - task.position  (если d <= 0, то coeff = 1.0)

NONE:     coeff = 1.0
SUBTRACT: coeff = max(0, 1 - penaltyStep * d)
MULTIPLY: coeff = penaltyStep ^ d

effectivePoints = grade.value * coeff
effectiveMax    = task.maxPoints * coeff
```
Задания с `isMandatory=false` (бонусные) — не учитываются в максимуме, только если сданы.

## Итоговые оценки предмета

`GET /api/subjects/{subjectId}/final-grades` возвращает итог по каждому студенту:
```typescript
{
  studentId: UUID
  username: string
  earnedPoints: number     // сумма (grade.value * coeff) по всем заданиям
  maxPoints: number        // сумма (task.maxPoints * coeff) только mandatory
  percentage: number | null  // earnedPoints / maxPoints * 100; null если maxPoints=0
}
```

## Таблица посещаемости предмета

`GET /api/subjects/{subjectId}/attendance` возвращает:
```typescript
{
  lessons: Array<{
    lessonId: UUID
    lessonName: string
    dateTime: string | null
    type: "LECTURE" | "PRACTICE" | "NONE"
    groupId: UUID | null
  }>
  students: Array<{ id: UUID, username: string }>
  attendances: Array<{
    attendanceId: UUID
    lessonId: UUID
    studentId: UUID
    presence: "NONE" | "PRESENT" | "NOT_PRESENT"
    note: string | null
  }>
}
```

**Query params:**
- `lessonType=LECTURE` — только лекции
- `lessonType=PRACTICE` — только практики
- `groupId=UUID` — фильтр по подгруппе

Фронт строит таблицу, матча `attendances` по `studentId` (строка) и `lessonId` (колонка).

---

## API Reference

### Teachers
| Метод | URL | Описание |
|-------|-----|----------|
| PUT | `/api/teachers/{id}` | Создать или обновить преподавателя (вызывать при логине). `id` = sub из JWT |

**Body:**
```json
{ "username": "string", "email": "string" }
```

---

### Subjects
| Метод | URL | Описание |
|-------|-----|----------|
| GET | `/api/subjects/teachers/{teacherId}` | Список предметов преподавателя |
| POST | `/api/subjects` | Создать предмет |
| PATCH | `/api/subjects/{subjectId}` | Обновить предмет (название, описание, archived) |
| DELETE | `/api/subjects/{subjectId}` | Удалить предмет |
| POST | `/api/subjects/{subjectId}/groups/{groupId}` | Прикрепить группу (добавляет студентов группы в предмет) |
| GET | `/api/subjects/{subjectId}/grades` | Полная таблица оценок |
| GET | `/api/subjects/{subjectId}/final-grades` | Итоговые оценки по студентам |

**Query params для GET /teachers/{teacherId}:**
- `archived=true` — показать заархивированные предметы

**POST /api/subjects body:**
```json
{ "name": "string", "description": "string|null", "teacherId": "UUID" }
```

**PATCH /api/subjects/{id} body** (все поля опциональны):
```json
{ "name": "string", "description": "string", "archived": true, "archivedAt": "ISO" }
```

---

### Lessons
| Метод | URL | Описание |
|-------|-----|----------|
| GET | `/api/lessons?subjectId={id}` | Список занятий предмета (Pageable) |
| POST | `/api/lessons` | Создать одно занятие |
| POST | `/api/lessons/bulk-schedule` | Массово создать занятия по недельному расписанию |
| PATCH | `/api/lessons/{id}` | Обновить занятие (частичное) |
| DELETE | `/api/lessons/{id}` | Удалить занятие |
| POST | `/api/lessons/{id}/issue` | Выдать занятие вручную (только для `issuanceMode=MANUAL`) |
| PATCH | `/api/lessons/{id}/issued-task-index` | Установить индекс активного задания |

**POST /api/lessons body:**
```json
{
  "name": "string",
  "dateTime": "2026-09-01T10:00:00+03:00",
  "type": "LECTURE | PRACTICE | NONE",
  "subjectId": "UUID",
  "groupId": "UUID | null",                // null = занятие для всей группы (лекция)
  "issuanceMode": "AUTO | MANUAL",       // default: AUTO
  "penaltyMode": "NONE | SUBTRACT | MULTIPLY", // default: NONE
  "penaltyStep": 0.25                     // default: 0.25
}
```

**PATCH /api/lessons/{id} body** (все поля опциональны):
```json
{
  "name": "string",
  "dateTime": "2026-09-01T10:00:00+03:00",
  "type": "LECTURE | PRACTICE | NONE",
  "groupId": "UUID | null",
  "issuanceMode": "AUTO | MANUAL",
  "penaltyMode": "NONE | SUBTRACT | MULTIPLY",
  "penaltyStep": 0.25
}
```

**POST /api/lessons/bulk-schedule body:**
```json
{
  "subjectId": "UUID",
  "schedules": [
    {
      "type": "LECTURE",
      "startDate": "2026-09-01",
      "totalCount": 16,
      "daysOfWeek": [
        ["MONDAY"],          // чётные недели
        ["WEDNESDAY"]        // нечётные недели
      ]
    }
  ]
}
```
`daysOfWeek` — массив недельных паттернов (чередуются). Один элемент = каждую неделю те же дни.

**PATCH /api/lessons/{id}/issued-task-index body:**
```json
{ "issuedTaskIndex": 2 }
```

---

### Lesson Tasks (Задания занятия)
| Метод | URL | Описание |
|-------|-----|----------|
| GET | `/api/lessons/{lessonId}/tasks` | Список заданий занятия (по position) |
| POST | `/api/lessons/{lessonId}/tasks` | Создать задание |
| PATCH | `/api/lessons/{lessonId}/tasks/{taskId}` | Обновить задание (частичное) |
| DELETE | `/api/lessons/{lessonId}/tasks/{taskId}` | Удалить задание |
| GET | `/api/lessons/{lessonId}/tasks/grades` | Оценки занятия (плоская таблица) |
| PUT | `/api/lessons/{lessonId}/tasks/{taskId}/grades` | Создать/обновить оценку студента |
| PUT | `/api/lessons/{lessonId}/tasks/{taskId}/grades/bulk` | Массовый upsert оценок |

**POST task body:**
```json
{
  "title": "Задача 1",
  "description": "string|null",
  "maxPoints": 10,
  "position": 0,
  "isMandatory": true,
  "deadline": "2026-09-15T23:59:00Z"
}
```

**PUT grade body:**
```json
{
  "studentId": "UUID",
  "value": 8,              // очки; null = не оценено
  "comment": "string|null",
  "status": "NOT_SUBMITTED | SUBMITTED | GRADED | RESUBMIT",
  "submittedAt": "ISO|null"
}
```

**PUT grades/bulk body** — массив объектов выше:
```json
[
  { "studentId": "UUID", "value": 8, "status": "GRADED" },
  { "studentId": "UUID", "value": 5, "status": "GRADED" }
]
```

**GET `/api/lessons/{lessonId}/tasks/grades` response:**
```json
{
  "students": [
    { "id": "UUID", "username": "string" }
  ],
  "grades": [
    {
      "id": "UUID",
      "taskId": "UUID",
      "lessonId": "UUID",
      "studentId": "UUID",
      "value": 8,
      "comment": null,
      "status": "GRADED",
      "submittedAt": "2026-09-10T12:00:00Z",
      "createdAt": "2026-09-01T10:00:00Z",
      "updatedAt": "2026-09-10T12:00:00Z"
    }
  ]
}
```

---

### Attendance (Посещаемость)
| Метод | URL | Описание |
|-------|-----|----------|
| GET | `/api/subjects/{subjectId}/attendance` | Таблица посещаемости предмета |
| PUT | `/api/lessons/{lessonId}/attendance` | Создать/обновить запись посещаемости |

**PUT attendance body:**
```json
{
  "studentId": "UUID",
  "presence": "PRESENT | NOT_PRESENT | NONE",
  "note": "string|null"
}
```

---

### Groups (Группы)
| Метод | URL | Описание |
|-------|-----|----------|
| GET | `/api/groups` | Список всех групп (пагинация) |
| POST | `/api/groups` | Создать группу со студентами |
| GET | `/api/groups/{groupId}` | Группа с подгруппами и студентами |
| PATCH | `/api/groups/{groupId}` | Переименовать группу |
| DELETE | `/api/groups/{groupId}` | Удалить группу (каскадно) |

**GET /api/groups query params:**
- `name=ИСТ` — фильтр по имени (содержит)
- `page=0&size=20&sort=name,asc`

**POST /api/groups body:**

Все студенты в главной группе (без подгрупп):
```json
{
  "groupName": "ИСТ-21",
  "students": [
    { "username": "Иванов И.И.", "subgroupIndex": null },
    { "username": "Петров П.П.", "subgroupIndex": null },
    { "username": "Сидоров С.С.", "subgroupIndex": null }
  ]
}
```

С подгруппами (`subgroupIndex` = 0 → "ИСТ-21/1", 1 → "ИСТ-21/2"):
```json
{
  "groupName": "ИСТ-21",
  "students": [
    { "username": "Иванов И.И.", "subgroupIndex": 0 },
    { "username": "Петров П.П.", "subgroupIndex": 0 },
    { "username": "Сидоров С.С.", "subgroupIndex": 1 },
    { "username": "Козлов К.К.", "subgroupIndex": 1 }
  ]
}
```

**GET /api/groups/{groupId} response:**
```json
{
  "id": "UUID",
  "name": "ИСТ-21",
  "subgroups": [
    { "id": "UUID", "name": "ИСТ-21/1" },
    { "id": "UUID", "name": "ИСТ-21/2" }
  ],
  "students": [
    { "id": "UUID", "username": "Иванов И.И.", "subgroupId": "UUID" },
    { "id": "UUID", "username": "Петров П.П.", "subgroupId": "UUID" },
    { "id": "UUID", "username": "Сидоров С.С.", "subgroupId": "UUID" },
    { "id": "UUID", "username": "Козлов К.К.", "subgroupId": "UUID" },
    { "id": "UUID", "username": "Попов П.П.", "subgroupId": null }
  ]
}
```

Фронт строит дерево, матча `students` по `subgroupId` против массива `subgroups`.

---

### Students (Студенты)
| Метод | URL | Описание |
|-------|-----|----------|
| GET | `/api/students` | Список студентов (пагинация) |
| POST | `/api/students` | Создать студента |
| PUT | `/api/students/{studentId}` | Обновить студента |
| DELETE | `/api/students/{studentId}` | Удалить студента |

**GET /api/students query params:**
- `username=Иванов` — фильтр по имени
- `groupId=UUID` — фильтр по группе
- `page=0&size=20`

---

## Страницы и маршруты приложения

```
/login                          → страница входа (OAuth2 redirect)
/                               → редирект на /subjects
/subjects                       → список предметов текущего преподавателя
/subjects/new                   → форма создания предмета
/subjects/:subjectId            → карточка предмета (вкладки ниже)
  /subjects/:subjectId/grades        → таблица оценок
  /subjects/:subjectId/attendance    → таблица посещаемости
  /subjects/:subjectId/lessons       → список занятий
  /subjects/:subjectId/final-grades  → итоговые оценки
/lessons/:lessonId              → детальная страница занятия (задания + оценки)
/groups                         → управление группами
/groups/:groupId                → группа: список студентов/подгрупп
```

---

## UX-нюансы

### Таблица оценок
- Колонки: `Студент | [Занятие 1: Задание 1 | Задание 2 | …] | [Занятие 2: …]`
- Ячейка = оценка студента за задание. Пусто = не выдавалась.
- Заблокированные задания (position < issuedTaskIndex) отображаются с коэффициентом штрафа
- Цветовая кодировка статуса: `NOT_SUBMITTED` (серый), `SUBMITTED` (жёлтый), `GRADED` (зелёный), `RESUBMIT` (оранжевый)

### Таблица посещаемости
- Колонки: `Студент | [Занятие 1] | [Занятие 2] | …`
- Ячейка: `PRESENT` (✓, зелёный), `NOT_PRESENT` (✗, красный), `NONE` (прочерк, серый)
- Клик по ячейке → меняет статус через PUT

### Занятия
- Тип занятия: `LECTURE` = лекция (для всей группы), `PRACTICE` = практика (для подгруппы)
- `issuanceMode=MANUAL` → в UI показывается кнопка "Выдать занятие"
- Индикатор активного задания (`issuedTaskIndex`) — выделяется в списке заданий

### Архивирование предметов
- Предметы можно архивировать через PATCH (`archived: true`)
- Список предметов: по умолчанию `archived=false`, переключатель "Показать архив"

---

## Pageable (пагинация)

Spring Pageable передаётся через query params:
```
?page=0&size=20&sort=name,asc
```
Ответ:
```typescript
{
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number    // текущая страница
}
```

---

## Обработка ошибок

Backend возвращает:
```typescript
{
  status: number
  message: string
  timestamp: string
  details: string | null   // дополнительная техническая информация (опционально)
}
```

| Код | Ситуация |
|-----|----------|
| 400 | Ошибки валидации |
| 401 | Истёк токен |
| 403 | Нет доступа (другой преподаватель) |
| 404 | Сущность не найдена |
| 500 | Ошибка сервера |
