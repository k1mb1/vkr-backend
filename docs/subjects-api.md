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
| `archived` | boolean | no | `true` — archived only; `false` — active only; omit — all |

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
