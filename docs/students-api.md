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

## PUT `/api/students/{studentId}`

Update a student.

**Path params**

| Param | Type | Description |
|---|---|---|
| `studentId` | UUID | Student identifier |

**Body** — `UpdateStudentRequest`

| Field | Type | Required | Notes |
|---|---|---|---|
| `name` | string | no | New display name |
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
