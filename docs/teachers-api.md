# Teachers API

Base URL: `/api/teachers`  
All responses are `application/json`.

---

## PUT `/api/teachers/{id}`

Create or update the authenticated teacher's profile (upsert on login).

**Authorization**: caller must be the same user as `{id}` (`isSameUser`).

**Path params**

| Param | Type | Description |
|---|---|---|
| `id` | UUID | Keycloak user id |

**Body** — `UpdateTeacherRequest`

| Field | Type | Required | Notes |
|---|---|---|---|
| `username` | string | no | Display name |
| `email` | string | no | Email address |

**Response `200`** — `TeacherResponse`

| Field | Type | Notes |
|---|---|---|
| `id` | UUID | |
| `username` | string | |
| `email` | string | |
| `createdAt` | string | ISO 8601 instant |
| `updatedAt` | string | ISO 8601 instant |
