# Student Task Grades API

Base URL: `/api/lessons/{lessonId}`  
All responses are `application/json`.

---

## GET `/api/lessons/{lessonId}/task-grades`

Get all task grades for a lesson, grouped by student.

**Path params**

| Param | Type | Description |
|---|---|---|
| `lessonId` | UUID | Lesson identifier |

**Response `200`** — `StudentTaskGradesResponse[]`

---

## PUT `/api/lessons/{lessonId}/tasks/{taskId}/grades`

Create or update a student's grade for a specific task (upsert).

**Path params**

| Param | Type | Description |
|---|---|---|
| `lessonId` | UUID | Lesson identifier |
| `taskId` | UUID | Task identifier |

**Body** — `UpsertTaskGradeRequest`

| Field | Type | Required | Notes |
|---|---|---|---|
| `studentId` | UUID | yes | |
| `value` | integer | no | Points awarded; `null` = not graded |
| `comment` | string | no | |
| `status` | `SubmissionStatus` | yes | See enum below |
| `submittedAt` | string | no | ISO 8601 instant; overrides auto-set timestamp |

**Response `200`** — `TaskGradeResponse`

---

## Types

### `SubmissionStatus`

| Value | Description |
|---|---|
| `NOT_SUBMITTED` | Student has not submitted |
| `SUBMITTED` | Submitted, awaiting grading |
| `GRADED` | Grade assigned |
| `RESUBMIT` | Teacher requested resubmission |

### `TaskGradeResponse`

| Field | Type | Notes |
|---|---|---|
| `id` | UUID | |
| `taskId` | UUID | |
| `studentId` | UUID | |
| `value` | number \| null | `null` = not graded |
| `comment` | string \| null | |
| `status` | `SubmissionStatus` | |
| `submittedAt` | string \| null | ISO 8601 instant; `null` when `NOT_SUBMITTED` |
| `createdAt` | string | ISO 8601 instant |
| `updatedAt` | string | ISO 8601 instant |

### `StudentTaskGradesResponse`

| Field | Type | Notes |
|---|---|---|
| `studentId` | UUID | |
| `username` | string | |
| `grades` | `TaskGradeResponse[]` | Ordered by task position |
