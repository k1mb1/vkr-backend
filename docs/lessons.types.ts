import type { UUID, Instant } from './common.types';

export type LessonType = 'NONE' | 'LECTURE' | 'PRACTICE';

export type DayOfWeek =
  | 'MONDAY'
  | 'TUESDAY'
  | 'WEDNESDAY'
  | 'THURSDAY'
  | 'FRIDAY'
  | 'SATURDAY'
  | 'SUNDAY';

/**
 * Controls when the lesson becomes visible and accessible to students.
 * - AUTO: automatically when the current time reaches `dateTime`.
 * - MANUAL: teacher calls POST /api/lessons/{id}/issue explicitly.
 */
export type IssuanceMode = 'AUTO' | 'MANUAL';

/**
 * How the displacement coefficient is computed for superseded tasks.
 * d = lesson.issuedTaskIndex - task.position
 *
 * - NONE:     coeff = 1.0  (no penalty — default)
 * - SUBTRACT: coeff = max(0, 1 - penaltyStep * d)
 * - MULTIPLY: coeff = penaltyStep ^ d
 */
export type PenaltyMode = 'NONE' | 'SUBTRACT' | 'MULTIPLY';

// ─── Responses ────────────────────────────────────────────────────────────────

export interface LessonResponse {
  id: UUID;
  name: string;
  /** ISO 8601 with offset, e.g. "2025-09-01T10:00:00+03:00" */
  dateTime: string | null;
  type: LessonType;
  subjectId: UUID;
  /** null for whole-cohort lessons */
  groupId: UUID | null;
  /** null when lesson targets the whole cohort or main group */
  subgroupNumber: number | null;
  issuanceMode: IssuanceMode;
  /** ISO 8601 instant; set when a MANUAL lesson is issued. null otherwise. */
  issuedAt: Instant | null;
  /**
   * Index of the task currently being presented to students.
   * Tasks with position < issuedTaskIndex are superseded and receive a
   * displacement penalty (computed by the front-end using penaltyMode/penaltyStep).
   */
  issuedTaskIndex: number;
  penaltyMode: PenaltyMode;
  /** Step value for the penalty. Ignored when penaltyMode is NONE. */
  penaltyStep: number;
  archived: boolean;
  archivedAt: Instant | null;
  createdAt: Instant;
  updatedAt: Instant;
}

// ─── Requests ─────────────────────────────────────────────────────────────────

export interface CreateLessonRequest {
  /** Non-blank */
  name: string;
  /** ISO 8601 with offset, optional */
  dateTime?: string;
  type: LessonType;
  subjectId: UUID;
  /** Defaults to AUTO when omitted */
  issuanceMode?: IssuanceMode;
  /** Defaults to NONE (no penalty) when omitted */
  penaltyMode?: PenaltyMode;
  /** Range (0.0001, 1.0]. Defaults to 0.25 when omitted. Ignored when penaltyMode is NONE. */
  penaltyStep?: number;
}

/** All fields optional; only provided fields are applied. */
export interface UpdateLessonRequest {
  name?: string;
  dateTime?: string;
  type?: LessonType;
  issuanceMode?: IssuanceMode;
  penaltyMode?: PenaltyMode;
  /** Range (0.0001, 1.0] */
  penaltyStep?: number;
}

export interface CreateLessonsByTypeRequest {
  subjectId: UUID;
  /** >= 0, at least one of lectureCount/practiceCount must be > 0 */
  lectureCount: number;
  /** >= 0, at least one of lectureCount/practiceCount must be > 0 */
  practiceCount: number;
}

export interface BulkScheduleEntry {
  type: LessonType;
  /** ISO 8601 date, e.g. "2025-09-01" */
  startDate: string;
  /** >= 1 */
  totalCount: number;
  /** Each inner list is a week pattern of days; at least one entry required */
  daysOfWeek: DayOfWeek[][];
}

export interface BulkScheduleRequest {
  subjectId: UUID;
  /** At least one entry required */
  schedules: BulkScheduleEntry[];
}

export interface UpdateIssuedTaskIndexRequest {
  /** >= 0. Tasks with position < issuedTaskIndex are considered superseded. */
  issuedTaskIndex: number;
}
