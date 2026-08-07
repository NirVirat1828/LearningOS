import type { CompletionStatus } from "../types/syllabus";

const STORAGE_KEY = "learningos.progress.v1";

export interface TopicProgressState {
  status: CompletionStatus;
  completionPercentage: number;
  completedDate: string | null;
  timeSpentMinutes: number | null;
}

export type ProgressStore = Record<string, TopicProgressState>;

const DEFAULT_PROGRESS: TopicProgressState = {
  status: "NOT_STARTED",
  completionPercentage: 0,
  completedDate: null,
  timeSpentMinutes: null,
};

/**
 * The only persistence layer in the backendless build: per-topic progress,
 * keyed by the topic's stable slug id. Never touches syllabus content —
 * see frontend/public/syllabi/README.md for why that split matters.
 */
function readStore(): ProgressStore {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    return raw ? (JSON.parse(raw) as ProgressStore) : {};
  } catch {
    return {};
  }
}

function writeStore(store: ProgressStore): void {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(store));
}

export function getAllProgress(): ProgressStore {
  return readStore();
}

export function getTopicProgress(topicId: string): TopicProgressState {
  return readStore()[topicId] ?? DEFAULT_PROGRESS;
}

export function setTopicProgress(topicId: string, progress: TopicProgressState): void {
  const store = readStore();
  store[topicId] = progress;
  writeStore(store);
}

/**
 * status/completionPercentage always move together; completedDate is
 * stamped on the transition into COMPLETED and cleared on any transition
 * away from it — same rule the old backend enforced server-side.
 */
export function updateTopicStatus(
  topicId: string,
  status: CompletionStatus,
  completionPercentage: number,
): TopicProgressState {
  const current = getTopicProgress(topicId);
  const next: TopicProgressState = {
    ...current,
    status,
    completionPercentage,
    completedDate: status === "COMPLETED" ? new Date().toISOString().slice(0, 10) : null,
  };
  setTopicProgress(topicId, next);
  return next;
}

/** Mark a topic done: stamps today and logs its planned time as time spent. */
export function completeTopic(topicId: string, estimatedMinutes: number): TopicProgressState {
  const next: TopicProgressState = {
    status: "COMPLETED",
    completionPercentage: 100,
    completedDate: new Date().toISOString().slice(0, 10),
    timeSpentMinutes: estimatedMinutes,
  };
  setTopicProgress(topicId, next);
  return next;
}

/** Undo completion, clearing the date and logged time so the calendar drops it. */
export function resetTopic(topicId: string): TopicProgressState {
  const next: TopicProgressState = {
    status: "NOT_STARTED",
    completionPercentage: 0,
    completedDate: null,
    timeSpentMinutes: null,
  };
  setTopicProgress(topicId, next);
  return next;
}
