export type LessonType = 'NONE' | 'LECTURE' | 'PRACTICE';

export type DayOfWeek =
  | 'MONDAY'
  | 'TUESDAY'
  | 'WEDNESDAY'
  | 'THURSDAY'
  | 'FRIDAY'
  | 'SATURDAY'
  | 'SUNDAY';

// Responses

export interface LessonResponse {
  id: string;
  name: string;
  /** ISO 8601 with offset, e.g. "2025-09-01T10:00:00+03:00" */
  dateTime: string | null;
  type: LessonType;
  subjectId: string;
  /** null for whole-cohort lessons (lectures) */
  groupId: string | null;
  /** null when lesson targets the whole cohort or main group */
  subgroupNumber: number | null;
  /** Decay coefficient [0.0001..1.0]. Front-end multiplies summed task scores by this value. */
  decayFactor: number;
  archived: boolean;
  archivedAt: string | null;
  createdAt: string;
  updatedAt: string;
}

// Requests

export interface CreateLessonRequest {
  /** Non-blank */
  name: string;
  /** ISO 8601 with offset, optional */
  dateTime?: string;
  type: LessonType;
  subjectId: string;
}

export interface CreateLessonsByTypeRequest {
  subjectId: string;
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
  subjectId: string;
  /** At least one entry required */
  schedules: BulkScheduleEntry[];
}

export interface UpdateDecayFactorRequest {
  /** Range (0.0001, 1.0]. 1.0 = no decay. */
  decayFactor: number;
}
