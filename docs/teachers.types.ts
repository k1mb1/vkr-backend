import type { UUID, Instant } from './common.types';

// ─── Requests ─────────────────────────────────────────────────────────────────

export interface UpdateTeacherRequest {
  username?: string;
  email?: string;
}

// ─── Responses ────────────────────────────────────────────────────────────────

export interface TeacherResponse {
  id: UUID;
  username: string;
  email: string;
  createdAt: Instant;
  updatedAt: Instant;
}
