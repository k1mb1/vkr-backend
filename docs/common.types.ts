// ─── Shared primitives ────────────────────────────────────────────────────────

/** UUID string, e.g. "550e8400-e29b-41d4-a716-446655440000" */
export type UUID = string;

/** ISO 8601 instant string, e.g. "2025-09-01T07:00:00Z" */
export type Instant = string;

// ─── Pagination ───────────────────────────────────────────────────────────────

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

// ─── Error ────────────────────────────────────────────────────────────────────

export interface ErrorDto {
  code: number;
  message: string;
  status: string;
  /** ISO 8601 instant */
  timestamp: Instant;
  details?: string;
}
