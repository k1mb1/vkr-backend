import type { UUID, Instant } from './common.types';

// ─── Requests ─────────────────────────────────────────────────────────────────

export interface CreateStudentRequest {
  username: string;
  groupId?: UUID;
}

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

export interface SubjectSubgroupStudentsResponse {
  id: UUID;
  name: string;
  studentNames: string[];
}

export interface StudentSubjectSubgroupsResponse {
  subjectId: UUID;
  subjectName: string;
  subgroups: SubjectSubgroupStudentsResponse[];
}

// ─── Filters ──────────────────────────────────────────────────────────────────

export interface FindStudentsFilter {
  username?: string;
  groupId?: UUID;
}
