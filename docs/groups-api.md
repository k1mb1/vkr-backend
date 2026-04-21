# Student Groups API

Base URL: `/api/groups`  
All responses are `application/json`.

---

## GET `/api/groups`

List all groups (paginated summary).

**Query params**

| Param | Type | Required | Notes |
|---|---|---|---|
| `name` | string | no | Filter by name (partial match) |
| `page` | integer | no | 0-based page index (default `0`) |
| `size` | integer | no | Page size (default `20`) |
| `sort` | string | no | e.g. `name,asc` |

**Response `200`** — `Page<StudentGroupPageResponse>`

---

## POST `/api/groups`

Create a group and enroll students.

**Body** — `CreateGroupRequest`

| Field | Type | Required | Notes |
|---|---|---|---|
| `groupName` | string | yes | Non-blank |
| `studentNames` | `string[][]` | yes | Non-empty. See note below. |

**`studentNames` rules**

- **One** inner array → all students are placed directly in the group (no subgroups).
- **N** inner arrays → N subgroups are auto-created: `"groupName/1"`, `"groupName/2"`, …, `"groupName/N"`.

**Response `200`** — `StudentGroupResponse`

---

## GET `/api/groups/{groupId}`

Get a group with its subgroups and students.

**Path params**

| Param | Type | Description |
|---|---|---|
| `groupId` | UUID | Group identifier |

**Response `200`** — `StudentGroupResponse`

---

## Types

### `StudentGroupPageResponse`

| Field | Type | Notes |
|---|---|---|
| `id` | UUID | |
| `name` | string | |
| `subgroupCount` | number | `0` when no subgroups |

### `StudentGroupResponse`

| Field | Type | Notes |
|---|---|---|
| `id` | UUID | |
| `name` | string | |
| `subjects` | `GroupSubjectResponse[]` | Attached subjects |
| `students` | `StudentEntryResponse[]` | Direct students; empty when group has subgroups |
| `subgroups` | `SubgroupResponse[]` | Empty when group has no subgroups |

### `SubgroupResponse`

| Field | Type | Notes |
|---|---|---|
| `id` | UUID | |
| `name` | string | e.g. `"ИСТ-21/2"` |
| `students` | `StudentEntryResponse[]` | |

### `GroupSubjectResponse`

| Field | Type | Notes |
|---|---|---|
| `id` | UUID | |
| `name` | string | |
