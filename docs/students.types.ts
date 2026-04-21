import type { UUID, Instant } from './common.types';

// ─── Requests ─────────────────────────────────────────────────────────────────

export interface UpdateStudentRequest {
  name?: string;
  groupId?: UUID;
}

// ─── Responses ────────────────────────────────────────────────────────────────

export interface StudentResponse {
  id: UUID;
  username: string;
  groupId: UUID | null;
  createdAt: Instant;
  updatedAt: Instant;
}

export interface StudentEntryResponse {
  id: UUID;
  username: string;
}

// ─── Filters ──────────────────────────────────────────────────────────────────

export interface FindStudentsFilter {
  username?: string;
  groupId?: UUID;
}
