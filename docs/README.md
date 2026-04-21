# API Documentation

## Endpoints

| File | Base path | Description |
|---|---|---|
| [teachers-api.md](teachers-api.md) | `/api/teachers` | Teacher profile upsert |
| [students-api.md](students-api.md) | `/api/students` | Student management |
| [groups-api.md](groups-api.md) | `/api/groups` | Student group management |
| [subjects-api.md](subjects-api.md) | `/api/subjects` | Subject management |
| [lessons-api.md](lessons-api.md) | `/api/lessons` | Lesson management |
| [lesson-tasks-api.md](lesson-tasks-api.md) | `/api/lessons/{lessonId}/tasks` | Lesson task management |
| [grades-api.md](grades-api.md) | `/api/lessons/{lessonId}` | Student task grades |

## TypeScript Types

| File | Contents |
|---|---|
| [common.types.ts](common.types.ts) | `UUID`, `Instant`, `Page<T>`, `ErrorDto` |
| [teachers.types.ts](teachers.types.ts) | Teacher request/response types |
| [students.types.ts](students.types.ts) | Student request/response types |
| [groups.types.ts](groups.types.ts) | Group request/response types |
| [subjects.types.ts](subjects.types.ts) | Subject request/response types |
| [lessons.types.ts](lessons.types.ts) | Lesson request/response types, `LessonType`, `DayOfWeek` |
| [lesson-tasks.types.ts](lesson-tasks.types.ts) | Task request/response types, `PenaltyMode` |
| [grades.types.ts](grades.types.ts) | Grade request/response types, `SubmissionStatus` |
