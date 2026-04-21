# Lesson Tasks API

Base URL: `/api/lessons/{lessonId}/tasks`  
All responses are `application/json`.

---

## GET `/api/lessons/{lessonId}/tasks`

List all tasks for a lesson, ordered by `position`.

**Path params**

| Param | Type | Description |
|---|---|---|
| `lessonId` | UUID | Lesson identifier |

**Response `200`** — `TaskResponse[]`

---

## POST `/api/lessons/{lessonId}/tasks`

Create a task for a lesson.

**Path params**

| Param | Type | Description |
|---|---|---|
| `lessonId` | UUID | Lesson identifier |

**Body** — `CreateTaskRequest`

| Field | Type | Required | Notes |
|---|---|---|---|
| `title` | string | yes | Non-blank |
| `description` | string | no | |
| `maxPoints` | integer | yes | `>= 1` |
| `position` | integer | yes | 0-based display position, `>= 0` |
| `issuedTaskIndex` | integer | yes | Index of the currently-active task, `>= 0` |
| `penaltyMode` | `PenaltyMode` | yes | `SUBTRACT` \| `MULTIPLY` |
| `penaltyStep` | number | yes | Range `(0.0001, 1.0]` |
| `isMandatory` | boolean | yes | Mandatory tasks always count in the total |
| `deadline` | string | no | ISO 8601 instant |

**Response `200`** — `TaskResponse`

---

## PATCH `/api/lessons/{lessonId}/tasks/{taskId}`

Partially update a task.

**Path params**

| Param | Type | Description |
|---|---|---|
| `lessonId` | UUID | Lesson identifier |
| `taskId` | UUID | Task identifier |

**Body** — `UpdateTaskRequest` (all fields optional)

| Field | Type | Notes |
|---|---|---|
| `title` | string | Non-blank |
| `description` | string | |
| `maxPoints` | integer | `>= 1` |
| `position` | integer | `>= 0` |
| `issuedTaskIndex` | integer | `>= 0` |
| `penaltyMode` | `PenaltyMode` | |
| `penaltyStep` | number | Range `(0.0001, 1.0]` |
| `isMandatory` | boolean | |
| `deadline` | string | ISO 8601 instant |

**Response `200`** — `TaskResponse`

---

## DELETE `/api/lessons/{lessonId}/tasks/{taskId}`

Delete a task from a lesson.

**Path params**

| Param | Type | Description |
|---|---|---|
| `lessonId` | UUID | Lesson identifier |
| `taskId` | UUID | Task identifier |

**Response `204`** — no content

---

## Types

### `PenaltyMode`

Controls how the displacement coefficient is computed when a task is superseded by newer tasks (`d = issuedTaskIndex - position`).

| Value | Formula |
|---|---|
| `SUBTRACT` | `coeff = max(0, 1 − penaltyStep × d)` |
| `MULTIPLY` | `coeff = penaltyStep ^ d` |

### `TaskResponse`

| Field | Type | Notes |
|---|---|---|
| `id` | UUID | |
| `lessonId` | UUID | |
| `title` | string | |
| `description` | string \| null | |
| `maxPoints` | number | |
| `position` | number | 0-based |
| `issuedTaskIndex` | number | |
| `penaltyMode` | `PenaltyMode` | |
| `penaltyStep` | number | Range `(0.0001, 1.0]` |
| `isMandatory` | boolean | |
| `deadline` | string \| null | ISO 8601 instant |
| `createdAt` | string | ISO 8601 instant |
| `updatedAt` | string | ISO 8601 instant |
