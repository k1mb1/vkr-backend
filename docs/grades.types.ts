import type { UUID, Instant } from './common.types';

// ─── Enums ────────────────────────────────────────────────────────────────────

export type SubmissionStatus = 'NOT_SUBMITTED' | 'SUBMITTED' | 'GRADED' | 'RESUBMIT';

// ─── Requests ─────────────────────────────────────────────────────────────────

export interface UpsertTaskGradeRequest {
  studentId: UUID;
  /** Points awarded; null = not graded */
  value?: number;
  comment?: string;
  status: SubmissionStatus;
  /** Overrides the auto-set submission timestamp when provided */
  submittedAt?: Instant;
}

// ─── Responses ────────────────────────────────────────────────────────────────

export interface TaskGradeResponse {
  id: UUID;
  taskId: UUID;
  studentId: UUID;
  /** null = not graded */
  value: number | null;
  comment: string | null;
  status: SubmissionStatus;
  /** null when status is NOT_SUBMITTED */
  submittedAt: Instant | null;
  createdAt: Instant;
  updatedAt: Instant;
}

export interface StudentTaskGradesResponse {
  studentId: UUID;
  username: string;
  /** Ordered by task position */
  grades: TaskGradeResponse[];
}
