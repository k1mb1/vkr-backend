# API Documentation

## Endpoints

| File | Base path | Description |
|---|---|---|
| [teachers-api.md](teachers-api.md) | `/api/teachers` | Teacher profile upsert |
| [students-api.md](students-api.md) | `/api/students` | Student management |
| [groups-api.md](groups-api.md) | `/api/groups` | Student group management |
| [subjects-api.md](subjects-api.md) | `/api/subjects` | Subject management, grades table, attendance table |
| [lessons-api.md](lessons-api.md) | `/api/lessons` | Lesson management |
| [lesson-tasks-api.md](lesson-tasks-api.md) | `/api/lessons/{lessonId}/tasks` | Task management and grade upsert |
| [attendance-api.md](attendance-api.md) | `/api/lessons/{lessonId}/attendance` | Attendance upsert |
| [grades-api.md](grades-api.md) | — | Grade endpoint index (see lesson-tasks-api.md) |

## TypeScript Types

| File | Contents |
|---|---|
| [common.types.ts](common.types.ts) | `UUID`, `Instant`, `Page<T>`, `ErrorDto` |
| [teachers.types.ts](teachers.types.ts) | Teacher request/response types |
| [students.types.ts](students.types.ts) | Student request/response types |
| [groups.types.ts](groups.types.ts) | Group request/response types |
| [subjects.types.ts](subjects.types.ts) | Subject request/response types |
| [lessons.types.ts](lessons.types.ts) | Lesson request/response types, `LessonType`, `DayOfWeek`, `IssuanceMode`, `PenaltyMode` |
| [lesson-tasks.types.ts](lesson-tasks.types.ts) | Task request/response types |
| [grades.types.ts](grades.types.ts) | Grade request/response types, `SubmissionStatus` |
| [attendance.types.ts](attendance.types.ts) | Attendance request/response types, `PresenceType` |

## Error responses

All endpoints return `ErrorDto` on failure:

| HTTP | Scenario |
|---|---|
| 400 | Validation failed — `details` lists field errors |
| 400 | Malformed JSON body |
| 400 | Invalid path/query parameter type |
| 403 | Access denied (wrong user) |
| 404 | Entity or resource not found |
| 500 | Unexpected server error |

See `ErrorDto` in [common.types.ts](common.types.ts).
