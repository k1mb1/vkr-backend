# Lessons API

Base URL: `/api/lessons`  
All responses are `application/json`.

---

## GET `/api/lessons/subjects/{subjectId}`

List all lessons for a subject.

**Path params**

| Param | Type | Description |
|---|---|---|
| `subjectId` | UUID | Subject identifier |

**Response `200`** — `LessonResponse[]`

---

## POST `/api/lessons`

Create a single lesson.

**Body** — `CreateLessonRequest`

| Field | Type | Required | Notes |
|---|---|---|---|
| `name` | string | yes | Non-blank |
| `dateTime` | string (ISO 8601) | no | e.g. `2025-09-01T10:00:00+03:00` |
| `type` | `LessonType` | yes | `NONE` \| `LECTURE` \| `PRACTICE` |
| `subjectId` | UUID | yes | |

**Response `200`** — `LessonResponse`

---

## POST `/api/lessons/bulk-by-type`

Create a batch of lessons by specifying counts per type (no dates assigned).

**Body** — `CreateLessonsByTypeRequest`

| Field | Type | Required | Notes |
|---|---|---|---|
| `subjectId` | UUID | yes | |
| `lectureCount` | integer | yes | `>= 0` |
| `practiceCount` | integer | yes | `>= 0` |

At least one of `lectureCount` / `practiceCount` must be `> 0`.

**Response `200`** — `LessonResponse[]`

---

## POST `/api/lessons/bulk-schedule`

Bulk-schedule recurring lessons from a start date following weekly day patterns.

**Body** — `BulkScheduleRequest`

| Field | Type | Required | Notes |
|---|---|---|---|
| `subjectId` | UUID | yes | |
| `schedules` | `BulkScheduleEntry[]` | yes | Non-empty |

**`BulkScheduleEntry`**

| Field | Type | Required | Notes |
|---|---|---|---|
| `type` | `LessonType` | yes | |
| `startDate` | string (ISO 8601 date) | yes | e.g. `2025-09-01` |
| `totalCount` | integer | yes | `>= 1` |
| `daysOfWeek` | `DayOfWeek[][]` | yes | Each inner array is one week's pattern; non-empty |

`DayOfWeek` values: `MONDAY` `TUESDAY` `WEDNESDAY` `THURSDAY` `FRIDAY` `SATURDAY` `SUNDAY`

**Response `200`** — `LessonResponse[]`

---

## PATCH `/api/lessons/{id}/decay-factor`

Update the decay factor for a lesson.

**Path params**

| Param | Type | Description |
|---|---|---|
| `id` | UUID | Lesson identifier |

**Body** — `UpdateDecayFactorRequest`

| Field | Type | Required | Notes |
|---|---|---|---|
| `decayFactor` | number | yes | Range `(0.0001, 1.0]`. `1.0` = no decay |

The front-end multiplies summed weighted task scores by this value (e.g. `0.5` halves all scores from the lesson).

**Response `200`** — `LessonResponse`

---

## Types

### `LessonType`

```
'NONE' | 'LECTURE' | 'PRACTICE'
```

### `LessonResponse`

| Field | Type | Notes |
|---|---|---|
| `id` | UUID | |
| `name` | string | |
| `dateTime` | string \| null | ISO 8601 with offset |
| `type` | `LessonType` | |
| `subjectId` | UUID | |
| `groupId` | UUID \| null | `null` for whole-cohort lessons |
| `subgroupNumber` | number \| null | Extracted from group name, e.g. `"ИСТ-21/2"` → `2` |
| `decayFactor` | number | `[0.0001, 1.0]` |
| `archived` | boolean | |
| `archivedAt` | string \| null | ISO 8601 instant |
| `createdAt` | string | ISO 8601 instant |
| `updatedAt` | string | ISO 8601 instant |
