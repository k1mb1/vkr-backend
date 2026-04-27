# Students API

Base URL: `/api/students`  
All responses are `application/json`.

---

## GET `/api/students`

List students with optional filtering and pagination.

**Query params**

| Param | Type | Required | Notes |
|---|---|---|---|
| `username` | string | no | Filter by username (partial match) |
| `groupId` | UUID | no | Filter by group |
| `page` | integer | no | 0-based page index (default `0`) |
| `size` | integer | no | Page size (default `20`) |
| `sort` | string | no | e.g. `username,asc` |

**Response `200`** — `Page<StudentResponse>`

---

## POST `/api/students`

Create a student.

**Body** — `CreateStudentRequest`

| Field | Type | Required | Notes |
|---|---|---|---|
| `username` | string | yes | Non-blank |
| `groupId` | UUID | no | Assign to this group immediately |

**Response `201`** — `StudentResponse`

---

## PUT `/api/students/{studentId}`

Update a student.

**Path params**

| Param | Type | Description |
|---|---|---|
| `studentId` | UUID | Student identifier |

**Body** — `UpdateStudentRequest`

| Field | Type | Required | Notes |
|---|---|---|---|
| `name` | string | no | New display name (returned as `username` in responses) |
| `groupId` | UUID | no | Move student to this group |

**Response `200`** — `StudentResponse`

---

## DELETE `/api/students/{studentId}`

Delete a student.

**Path params**

| Param | Type | Description |
|---|---|---|
| `studentId` | UUID | Student identifier |

**Response `204`** — no content

---

## GET `/api/students/subjects/{id}`

Get a subject with subgroup student lists.

**Path params**

| Param | Type | Description |
|---|---|---|
| `id` | UUID | Subject identifier |

**Response `200`** — `StudentSubjectSubgroupsResponse`

---

## Types

### `StudentResponse`

| Field | Type | Notes |
|---|---|---|
| `id` | UUID | |
| `username` | string | |
| `groupId` | UUID \| null | |
| `createdAt` | string | ISO 8601 instant |
| `updatedAt` | string | ISO 8601 instant |

### `StudentEntryResponse`

| Field | Type | Notes |
|---|---|---|
| `id` | UUID | |
| `username` | string | |

### `StudentSubjectSubgroupsResponse`

| Field | Type | Notes |
|---|---|---|
| `subjectId` | UUID | |
| `subjectName` | string | |
| `subgroups` | `SubjectSubgroupStudentsResponse[]` | Sorted by subgroup name |

### `SubjectSubgroupStudentsResponse`

| Field | Type | Notes |
|---|---|---|
| `id` | UUID | Subgroup/group identifier |
| `name` | string | Subgroup/group name |
| `studentNames` | `string[]` | Sorted student names |
