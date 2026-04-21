import type { UUID, Instant } from './common.types';

// ─── Requests ─────────────────────────────────────────────────────────────────

export interface CreateSubjectRequest {
  /** Non-blank */
  name: string;
  description?: string;
  teacherId: UUID;
}

// ─── Responses ────────────────────────────────────────────────────────────────

export interface SubjectResponse {
  id: UUID;
  name: string;
  description: string | null;
  archived: boolean;
  archivedAt: Instant | null;
  createdAt: Instant;
  updatedAt: Instant;
}

export interface AttachGroupToSubjectResponse {
  subjectId: UUID;
  subjectName: string;
  groupId: UUID;
  groupName: string;
  addedStudentsCount: number;
  totalStudentsInSubject: number;
}

// ─── Filters ──────────────────────────────────────────────────────────────────

export interface FindSubjectsFilter {
  archived?: boolean;
}
