import type { UUID, Instant } from './common.types';

// ─── Requests ─────────────────────────────────────────────────────────────────

export interface CreateSubjectRequest {
  /** Non-blank */
  name: string;
  description?: string;
  teacherId: UUID;
}

export interface UpdateSubjectRequest {
  name?: string;
  description?: string;
  archived?: boolean;
  archivedAt?: Instant;
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

export interface FinalGradeResponse {
  studentId: UUID;
  username: string;
  earnedPoints: number;
  maxPoints: number;
  percentage: number | null;
}

// ─── Filters ──────────────────────────────────────────────────────────────────

export interface FindSubjectsFilter {
  archived?: boolean;
}
