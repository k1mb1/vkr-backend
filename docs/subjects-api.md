# Subjects API

Base URL: `/api/subjects`  
All responses are `application/json`.

---

## GET `/api/subjects/teachers/{teacherId}`

List subjects belonging to a teacher.

**Authorization**: caller must be the same user as `{teacherId}` (`isSameUser`).

**Path params**

| Param | Type | Description |
|---|---|---|
| `teacherId` | UUID | Teacher identifier |

**Query params**

| Param | Type | Required | Notes |
|---|---|---|---|
| `archived` | boolean | no | `true` — archived only; `false` — active only; omit — active only |

**Response `200`** — `SubjectResponse[]`

---

## POST `/api/subjects`

Create a subject.

**Authorization**: caller must be the same user as `request.teacherId` (`isSameUser`).

**Body** — `CreateSubjectRequest`

| Field | Type | Required | Notes |
|---|---|---|---|
| `name` | string | yes | Non-blank |
| `description` | string | no | |
| `teacherId` | UUID | yes | |

**Response `201`** — `SubjectResponse`

---

## PATCH `/api/subjects/{subjectId}`

Update subject metadata. All fields optional.

**Body** — `UpdateSubjectRequest`

| Field | Type | Notes |
|---|---|---|
| `name` | string | New name |
| `description` | string | New description |
| `archived` | boolean | Archive flag |
| `archivedAt` | string | ISO 8601 instant |

**Response `200`** — `SubjectResponse`

---

## PATCH `/api/subjects/{subjectId}/archive`

Archive a subject.

**Path params**

| Param | Type | Description |
|---|---|---|
| `subjectId` | UUID | Subject identifier |

**Response `200`** — `SubjectResponse` (with `archived: true` and `archivedAt` set)

---

## POST `/api/subjects/{subjectId}/groups/{groupId}`

Attach a full group (and all its students) to a subject.

**Path params**

| Param | Type | Description |
|---|---|---|
| `subjectId` | UUID | Subject identifier |
| `groupId` | UUID | Group identifier |

**Response `200`** — `AttachGroupToSubjectResponse`

---

## GET `/api/subjects/{subjectId}/grades`

Full grades table for a subject — all students, all tasks across all lessons.

**Path params**

| Param | Type | Description |
|---|---|---|
| `subjectId` | UUID | Subject identifier |

**Response `200`** — `SubjectGradesTableResponse`

The response contains:
- `lessons` — all subject lessons (for table columns)
- `students` — all students enrolled in the subject (for table rows)

Grades inside each student entry are sorted by lesson date, then by task position within each lesson.

---

## GET `/api/subjects/{subjectId}/final-grades`

Aggregated final grade per student for a subject.

For every enrolled student computes:
- `earnedPoints` — sum of `grade.value × displacementCoeff` for all graded tasks (ungraded mandatory tasks contribute 0)
- `maxPoints` — sum of `task.maxPoints × displacementCoeff` for mandatory tasks only
- `percentage` — `earnedPoints / maxPoints × 100` (`null` when `maxPoints = 0`)

**Displacement formula** per task (`d = lesson.issuedTaskIndex − task.position`, `d ≤ 0 → coeff = 1.0`):

| `penaltyMode` | Formula |
|---|---|
| `NONE` | `coeff = 1.0` |
| `SUBTRACT` | `coeff = max(0, 1 − penaltyStep × d)` |
| `MULTIPLY` | `coeff = penaltyStep ^ d` |

**Response `200`** — `FinalGradeResponse[]`

### `FinalGradeResponse`

| Field | Type | Notes |
|---|---|---|
| `studentId` | UUID | |
| `username` | string | |
| `earnedPoints` | number | |
| `maxPoints` | number | |
| `percentage` | number \| null | `null` when `maxPoints = 0` |

---

## GET `/api/subjects/{subjectId}/attendance`

Full attendance table for a subject — all students, all lessons.

**Path params**

| Param | Type | Description |
|---|---|---|
| `subjectId` | UUID | Subject identifier |

**Response `200`** — `SubjectAttendanceTableResponse`

See [attendance-api.md](attendance-api.md) for the response shape.

---

## Types

### `SubjectResponse`

| Field | Type | Notes |
|---|---|---|
| `id` | UUID | |
| `name` | string | |
| `description` | string \| null | |
| `archived` | boolean | |
| `archivedAt` | string \| null | ISO 8601 instant |
| `createdAt` | string | ISO 8601 instant |
| `updatedAt` | string | ISO 8601 instant |

### `AttachGroupToSubjectResponse`

| Field | Type | Notes |
|---|---|---|
| `subjectId` | UUID | |
| `subjectName` | string | |
| `groupId` | UUID | |
| `groupName` | string | |
| `addedStudentsCount` | number | Students newly enrolled |
| `totalStudentsInSubject` | number | Total after attach |
