import type { UUID, Instant } from './common.types';

// ─── Enums ────────────────────────────────────────────────────────────────────

/**
 * Controls how the displacement coefficient is computed when a task is
 * superseded by newer tasks (d = issuedTaskIndex - position).
 *
 * - SUBTRACT: coeff = max(0, 1 - penaltyStep * d)
 * - MULTIPLY: coeff = penaltyStep ^ d
 */
export type PenaltyMode = 'SUBTRACT' | 'MULTIPLY';

// ─── Requests ─────────────────────────────────────────────────────────────────

export interface CreateTaskRequest {
  /** Non-blank */
  title: string;
  description?: string;
  /** >= 1 */
  maxPoints: number;
  /** 0-based display position, >= 0 */
  position: number;
  /** Index of the currently-active (latest issued) task, >= 0 */
  issuedTaskIndex: number;
  penaltyMode: PenaltyMode;
  /** Range (0.0001, 1.0] */
  penaltyStep: number;
  /** Mandatory tasks always count in the total. Default: true */
  isMandatory: boolean;
  /** Submission deadline, optional */
  deadline?: Instant;
}

export interface UpdateTaskRequest {
  title?: string;
  description?: string;
  /** >= 1 */
  maxPoints?: number;
  /** >= 0 */
  position?: number;
  /** >= 0 */
  issuedTaskIndex?: number;
  penaltyMode?: PenaltyMode;
  /** Range (0.0001, 1.0] */
  penaltyStep?: number;
  isMandatory?: boolean;
  deadline?: Instant;
}

// ─── Responses ────────────────────────────────────────────────────────────────

export interface TaskResponse {
  id: UUID;
  lessonId: UUID;
  title: string;
  description: string | null;
  maxPoints: number;
  position: number;
  issuedTaskIndex: number;
  penaltyMode: PenaltyMode;
  /** Range (0.0001, 1.0] */
  penaltyStep: number;
  isMandatory: boolean;
  deadline: Instant | null;
  createdAt: Instant;
  updatedAt: Instant;
}
