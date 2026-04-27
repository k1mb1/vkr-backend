# Lesson Tasks API

Base URL: `/api/lessons/{lessonId}/tasks`  
All responses are `application/json`.

---

## GET `/api/lessons/{lessonId}/tasks`

List all tasks for a lesson, ordered by `position`.

**Response `200`** — `TaskResponse[]`

---

## POST `/api/lessons/{lessonId}/tasks`

Create a task for a lesson.

**Body** — `CreateTaskRequest`

| Field | Type | Required | Notes |
|---|---|---|---|
| `title` | string | yes | Non-blank |
| `description` | string | no | |
| `maxPoints` | integer | yes | `>= 1` |
| `position` | integer | yes | 0-based display position, `>= 0` |
| `isMandatory` | boolean | yes | Mandatory tasks always count in the total |
| `deadline` | string | no | ISO 8601 instant |

**Response `201`** — `TaskResponse`

---

## PATCH `/api/lessons/{lessonId}/tasks/{taskId}`

Partially update a task. All fields optional.

**Body** — `UpdateTaskRequest`

| Field | Type | Notes |
|---|---|---|
| `title` | string | Non-blank |
| `description` | string | |
| `maxPoints` | integer | `>= 1` |
| `position` | integer | `>= 0` |
| `isMandatory` | boolean | |
| `deadline` | string | ISO 8601 instant |

**Response `200`** — `TaskResponse`

---

## DELETE `/api/lessons/{lessonId}/tasks/{taskId}`

Delete a task from a lesson.

**Response `204`** — no content

---

## GET `/api/lessons/{lessonId}/tasks/grades`

Get all task grades for a lesson, grouped by student.

**Response `200`** — `StudentTaskGradesResponse[]`

---

## PUT `/api/lessons/{lessonId}/tasks/{taskId}/grades`

Create or update a student's grade for a specific task (upsert).

**Body** — `UpsertTaskGradeRequest`

| Field | Type | Required | Notes |
|---|---|---|---|
| `studentId` | UUID | yes | |
| `value` | integer \| null | no | Points awarded; `null` = not graded |
| `comment` | string | no | |
| `status` | `SubmissionStatus` | yes | |
| `submittedAt` | Instant | no | ISO 8601 instant; overrides auto-set timestamp |

**Response `200`** — `TaskGradeResponse`

---

## PUT `/api/lessons/{lessonId}/tasks/{taskId}/grades/bulk`

Upsert grades for multiple students in a single request.  
Entries are processed independently; the response list preserves input order.

**Body** — `UpsertTaskGradeRequest[]`

**Response `200`** — `TaskGradeResponse[]`

---

## Types

> Displacement penalty configuration (`issuedTaskIndex`, `penaltyMode`, `penaltyStep`) is stored
> on the **lesson**, not on individual tasks. Fetch it from `LessonResponse` and compute per task:
>
> `d = lesson.issuedTaskIndex - task.position`  
> `NONE: coeff = 1.0` · `SUBTRACT: coeff = max(0, 1 − penaltyStep × d)` · `MULTIPLY: coeff = penaltyStep ^ d`

### `SubmissionStatus`

| Value | Description |
|---|---|
| `NOT_SUBMITTED` | Student has not submitted |
| `SUBMITTED` | Submitted, awaiting grading |
| `GRADED` | Grade assigned |
| `RESUBMIT` | Teacher requested resubmission |

### `TaskResponse`

| Field | Type | Notes |
|---|---|---|
| `id` | UUID | |
| `lessonId` | UUID | |
| `title` | string | |
| `description` | string \| null | |
| `maxPoints` | number | |
| `position` | number | 0-based |
| `isMandatory` | boolean | |
| `deadline` | Instant \| null | ISO 8601 instant |
| `createdAt` | Instant | ISO 8601 instant |
| `updatedAt` | Instant | ISO 8601 instant |

### `TaskGradeResponse`

| Field | Type | Notes |
|---|---|---|
| `id` | UUID | |
| `taskId` | UUID | |
| `lessonId` | UUID | |
| `studentId` | UUID | |
| `value` | number \| null | `null` = not graded |
| `comment` | string \| null | |
| `status` | `SubmissionStatus` | |
| `submittedAt` | Instant \| null | ISO 8601 instant |
| `createdAt` | Instant | ISO 8601 instant |
| `updatedAt` | Instant | ISO 8601 instant |

### `StudentTaskGradesResponse`

| Field | Type | Notes |
|---|---|---|
| `studentId` | UUID | |
| `username` | string | |
| `grades` | `TaskGradeResponse[]` | Ordered by task position |
