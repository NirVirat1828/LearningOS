import type { CompletionStatus, Difficulty, TopicPriority } from "../types/syllabus";

export const DIFFICULTY_STYLES: Record<Difficulty, string> = {
  BEGINNER: "bg-emerald-100 text-emerald-700",
  INTERMEDIATE: "bg-amber-100 text-amber-700",
  ADVANCED: "bg-rose-100 text-rose-700",
};

export const STATUS_STYLES: Record<CompletionStatus, string> = {
  NOT_STARTED: "bg-slate-100 text-slate-600",
  IN_PROGRESS: "bg-blue-100 text-blue-700",
  COMPLETED: "bg-emerald-100 text-emerald-700",
};

/** Only "LATER" ever renders — "ACTIVE" is the unlabeled default. */
export const PRIORITY_STYLES: Record<TopicPriority, string> = {
  ACTIVE: "",
  LATER: "bg-violet-100 text-violet-700",
};

export function toTitleCase(value: string): string {
  return value
    .toLowerCase()
    .split("_")
    .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
    .join(" ");
}

export function formatEstimatedTime(minutes: number): string {
  if (minutes < 60) {
    return `${minutes} min`;
  }
  const hours = Math.floor(minutes / 60);
  const remainder = minutes % 60;
  return remainder === 0 ? `${hours} hr` : `${hours} hr ${remainder} min`;
}
