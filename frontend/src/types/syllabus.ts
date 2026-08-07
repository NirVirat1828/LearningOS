import type { Resource } from "./resources";

export type Difficulty = "BEGINNER" | "INTERMEDIATE" | "ADVANCED";

export type CompletionStatus = "NOT_STARTED" | "IN_PROGRESS" | "COMPLETED";

/**
 * ids are stable slugs (e.g. "system-design/load-balancing/health-checks"),
 * not UUIDs — see frontend/public/syllabi/README.md. That stability is what
 * lets localStorage progress survive a syllabus file being regenerated.
 */
export interface Topic {
  id: string;
  moduleId: string;
  title: string;
  description: string | null;
  difficulty: Difficulty;
  estimatedMinutes: number;
  completionStatus: CompletionStatus;
  completionPercentage: number;
  completedDate: string | null;
  /** Topic ids that must be completed first. Drives the roadmap graph and planner ordering. */
  prerequisites: string[];
  resources: Resource[];
}

export interface SyllabusModule {
  id: string;
  courseId: string;
  title: string;
  description: string | null;
  topics: Topic[];
}

export interface Course {
  id: string;
  roadmapId: string;
  title: string;
  description: string | null;
  modules: SyllabusModule[];
}

export interface Roadmap {
  id: string;
  title: string;
  description: string | null;
  courses: Course[];
}
