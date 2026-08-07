import type { Roadmap, Topic } from "../types/syllabus";
import { loadRoadmaps, loadTopic } from "../lib/syllabusData";

// Backendless build: these read static JSON from /public/syllabi + localStorage
// progress (see lib/syllabusData.ts) instead of calling a REST API. Kept as
// thin wrappers here so the hooks importing them didn't need to change shape.

export async function fetchRoadmaps(): Promise<Roadmap[]> {
  return loadRoadmaps();
}

export async function fetchTopic(topicId: string): Promise<Topic> {
  return loadTopic(topicId);
}
