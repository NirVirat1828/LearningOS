import type { Topic } from "../types/syllabus";
import { loadRoadmaps } from "./syllabusData";
import { getDailyMinutes, getStoredPlan, setStoredPlan, todayIso } from "./localPlan";

/** Flattens every topic across all roadmaps, preserving authored order. */
function flattenTopics(roadmaps: Awaited<ReturnType<typeof loadRoadmaps>>): Topic[] {
  const topics: Topic[] = [];
  for (const roadmap of roadmaps) {
    for (const course of roadmap.courses) {
      for (const module of course.modules) {
        for (const topic of module.topics) {
          topics.push(topic);
        }
      }
    }
  }
  return topics;
}

export interface TodaysPlan {
  date: string;
  topics: Topic[];
  dailyMinutes: number;
}

/**
 * The adaptive rule-based planner. No day-by-day schedule is stored ahead of
 * time — today's list is derived each day from three inputs:
 *   1. the topic pool (static syllabus JSON),
 *   2. progress (localStorage), and
 *   3. the daily time budget.
 *
 * A topic is "actionable" when it isn't COMPLETED, isn't priority "LATER"
 * (backlog items sit out of the active plan until pulled in manually via the
 * syllabus browser), and every prerequisite IS COMPLETED. We take actionable
 * topics in authored order and add them until their estimated minutes reach
 * the budget. That prerequisite gate is what
 * makes a sprint's cadence emerge for free: e.g. DSA Day 2 stays locked until
 * all of Day 1 is done, so a day's budget naturally yields ~a day's problems.
 *
 * Once computed, the plan is persisted for the date so it stays a stable,
 * finishable list; completing a topic marks it done in place rather than
 * shuffling the list.
 */
export async function computeTodaysPlan(): Promise<TodaysPlan> {
  const date = todayIso();
  const dailyMinutes = getDailyMinutes();
  const roadmaps = await loadRoadmaps();
  const allTopics = flattenTopics(roadmaps);
  const byId = new Map(allTopics.map((t) => [t.id, t]));

  const stored = getStoredPlan(date);
  if (stored) {
    // Resolve to current topics (dropping any ids no longer in the syllabi).
    const topics = stored.map((id) => byId.get(id)).filter((t): t is Topic => Boolean(t));
    return { date, topics, dailyMinutes };
  }

  const completed = new Set(allTopics.filter((t) => t.completionStatus === "COMPLETED").map((t) => t.id));

  // Preserve topics already finished *today* so a re-plan (e.g. changing the
  // budget) keeps today's wins visible instead of dropping them from the list.
  const completedToday = allTopics.filter(
    (t) => t.completionStatus === "COMPLETED" && t.completedDate === date,
  );
  const alreadyIn = new Set(completedToday.map((t) => t.id));

  const actionable = allTopics.filter(
    (t) =>
      !completed.has(t.id) &&
      !alreadyIn.has(t.id) &&
      t.priority !== "LATER" &&
      t.prerequisites.every((p) => completed.has(p)),
  );

  // First-fit that respects the budget as a ceiling (never overshoot), but
  // always includes at least one topic so a single big topic still shows up
  // even when it alone exceeds the daily budget.
  const pending: Topic[] = [];
  let usedMinutes = 0;
  for (const topic of actionable) {
    if (pending.length === 0 || usedMinutes + topic.estimatedMinutes <= dailyMinutes) {
      pending.push(topic);
      usedMinutes += topic.estimatedMinutes;
    }
  }

  const selected = [...completedToday, ...pending];
  setStoredPlan(date, selected.map((t) => t.id));
  return { date, topics: selected, dailyMinutes };
}
