import type { Course, Roadmap, SyllabusModule, Topic } from "../types/syllabus";
import type { Resource, ResourceType } from "../types/resources";
import { getTopicProgress } from "./localProgress";

interface ManifestRoadmap {
  id: string;
  title: string;
  description: string;
  courses: string[];
}

interface Manifest {
  roadmaps: ManifestRoadmap[];
}

interface RawResource {
  title: string;
  url: string;
  type: ResourceType;
}

interface RawTopic {
  id: string;
  title: string;
  difficulty: Topic["difficulty"];
  estimatedMinutes: number;
  prerequisites?: string[];
  resources?: RawResource[];
}

interface RawModule {
  id: string;
  title: string;
  description?: string;
  topics: RawTopic[];
}

interface RawCourse {
  id: string;
  title: string;
  description?: string;
  modules: RawModule[];
}

async function fetchJson<T>(path: string): Promise<T> {
  const response = await fetch(path);
  // A dev-server SPA fallback (or any static host with a catch-all route)
  // can return index.html with a 200 for a path that doesn't exist, so
  // response.ok alone isn't proof the file was actually found — check the
  // content type too, so a missing/mistyped manifest entry fails with a
  // clear message instead of a cryptic JSON-parse error.
  const contentType = response.headers.get("content-type") ?? "";
  if (!response.ok || !contentType.includes("json")) {
    throw new Error(`Failed to load ${path}: ${response.status} (content-type: ${contentType || "unknown"})`);
  }
  return response.json() as Promise<T>;
}

function toResource(raw: RawResource, topicId: string, index: number): Resource {
  return {
    id: `${topicId}::r${index}`,
    title: raw.title,
    url: raw.url,
    type: raw.type,
  };
}

function toTopic(raw: RawTopic, moduleId: string): Topic {
  const progress = getTopicProgress(raw.id);
  return {
    id: raw.id,
    moduleId,
    title: raw.title,
    description: null,
    difficulty: raw.difficulty,
    estimatedMinutes: raw.estimatedMinutes,
    completionStatus: progress.status,
    completionPercentage: progress.completionPercentage,
    completedDate: progress.completedDate,
    prerequisites: raw.prerequisites ?? [],
    resources: (raw.resources ?? []).map((r, i) => toResource(r, raw.id, i)),
  };
}

function toModule(raw: RawModule, courseId: string): SyllabusModule {
  return {
    id: raw.id,
    courseId,
    title: raw.title,
    description: raw.description ?? null,
    topics: raw.topics.map((t) => toTopic(t, raw.id)),
  };
}

async function loadCourse(courseFileId: string, roadmapId: string): Promise<Course> {
  const raw = await fetchJson<RawCourse>(`/syllabi/${courseFileId}.json`);
  return {
    id: raw.id,
    roadmapId,
    title: raw.title,
    description: raw.description ?? null,
    modules: raw.modules.map((m) => toModule(m, raw.id)),
  };
}

/**
 * Loads every roadmap + course from the static /syllabi files, re-reading
 * localStorage progress fresh each call so a just-completed topic shows up
 * immediately on refetch (React Query cache invalidation is what triggers
 * that refetch — see hooks/useRoadmaps.ts and useUpdateTopicProgress.ts).
 */
export async function loadRoadmaps(): Promise<Roadmap[]> {
  const manifest = await fetchJson<Manifest>("/syllabi/manifest.json");

  return Promise.all(
    manifest.roadmaps.map(async (r) => ({
      id: r.id,
      title: r.title,
      description: r.description ?? null,
      courses: await Promise.all(r.courses.map((courseId) => loadCourse(courseId, r.id))),
    })),
  );
}

export async function loadTopic(topicId: string): Promise<Topic> {
  const roadmaps = await loadRoadmaps();
  for (const roadmap of roadmaps) {
    for (const course of roadmap.courses) {
      for (const module of course.modules) {
        const topic = module.topics.find((t) => t.id === topicId);
        if (topic) {
          return topic;
        }
      }
    }
  }
  throw new Error(`Topic not found: ${topicId}`);
}
