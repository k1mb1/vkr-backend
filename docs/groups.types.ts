import type { UUID } from './common.types';
import type { StudentEntryResponse } from './students.types';

// ─── Requests ─────────────────────────────────────────────────────────────────

export interface CreateGroupRequest {
  groupName: string;
  /**
   * Each inner array is a list of student usernames.
   * - One inner array → all students placed directly in the group.
   * - N inner arrays → N subgroups auto-created: "groupName/1" … "groupName/N".
   */
  studentNames: string[][];
}

export interface UpdateGroupRequest {
  name: string;
}

// ─── Responses ────────────────────────────────────────────────────────────────

export interface GroupSubjectResponse {
  id: UUID;
  name: string;
}

export interface SubgroupResponse {
  id: UUID;
  name: string;
  students: StudentEntryResponse[];
}

export interface StudentGroupResponse {
  id: UUID;
  name: string;
  subjects: GroupSubjectResponse[];
  /** Direct students; empty when the group has subgroups. */
  students: StudentEntryResponse[];
  /** Empty when the group has no subgroups. */
  subgroups: SubgroupResponse[];
}

export interface StudentGroupPageResponse {
  id: UUID;
  name: string;
  subgroupCount: number;
}

// ─── Filters ──────────────────────────────────────────────────────────────────

export interface FindGroupsFilter {
  name?: string;
}
