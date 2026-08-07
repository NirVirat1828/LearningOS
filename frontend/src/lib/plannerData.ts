import type { Difficulty, Topic } from "../types/syllabus";
import { loadRoadmaps } from "./syllabusData";
import { getAllProgress } from "./localProgress";
import {
  getDailyMinutes,
  getStoredPlan,
  setStoredPlan,
  todayIso,
  type StoredPlan,
  type StoredPlanSection,
} from "./localPlan";

export type PlanSectionKind = "REVIEW" | "FOCUS" | "VARIETY";

/** One coherent block of the day, with a one-line reason so nothing feels random. */
export interface PlanSection {
  kind: PlanSectionKind;
  /** Module title (FOCUS/VARIETY) or a fixed label (REVIEW). */
  title: string;
  /** The course the block belongs to, shown as a subtitle; null for REVIEW. */
  courseTitle: string | null;
  /** "Why this" — the pedagogical reason this block is today's move. */
  reason: string;
  topics: Topic[];
}

export interface TodaysPlan {
  date: string;
  dailyMinutes: number;
  sections: PlanSection[];
  /**
   * FOCUS + VARIETY topics flattened (REVIEW excluded — those are already done).
   * This is the day's *new learning*, and it's what the daily progress bar counts.
   */
  topics: Topic[];
}

const DIFFICULTY_RANK: Record<Difficulty, number> = {
  BEGINNER: 0,
  INTERMEDIATE: 1,
  ADVANCED: 2,
};

/**
 * Minutes held back from the focus block for the variety spark, so the day packs
 * well while focus still gets the bulk. Focus is also floored at half the day so
 * it always dominates even when the reserve is large relative to the budget.
 */
const VARIETY_RESERVE = 30;
/** Below this budget the day stays a pure focus block (no review, no variety). */
const MIN_BUDGET_FOR_EXTRAS = 45;
/** A variety spark is a single taste of another track, not a second focus. */
const MAX_VARIETY_TOPICS = 1;
/** Spaced-review window: resurface a hard topic finished this many days ago. */
const REVIEW_MIN_DAYS = 3;
const REVIEW_MAX_DAYS = 10;
/** A review is a quick recall pass, so it only ever costs this much of the budget. */
const REVIEW_MAX_COST = 15;

/**
 * The track the deep-focus block is biased toward: GeoIntel AI is the primary
 * mastery goal, so its modules lead focus selection over every other course.
 * Other tracks still advance daily via the variety spark, and take over the
 * focus block only once the AI track has no actionable modules left.
 */
const FOCUS_BIAS_COURSE_ID = "geointel-artificial-intelligence";

const MS_PER_DAY = 86_400_000;

interface ModuleInfo {
  moduleId: string;
  moduleTitle: string;
  courseId: string;
  courseTitle: string;
  /** Authored position across every module — lower means earlier in the curriculum. */
  order: number;
  topics: Topic[];
  actionable: Topic[];
  completedCount: number;
  totalCount: number;
  /** Started but not finished — the prime "finish what you started" candidates. */
  inProgress: boolean;
}

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

/**
 * A topic is "actionable" when it isn't COMPLETED, isn't priority "LATER"
 * (backlog items sit out of the active plan until pulled in manually via the
 * syllabus browser), and every prerequisite IS COMPLETED. That prerequisite
 * gate is what makes a sprint's cadence emerge for free: e.g. DSA Day 2 stays
 * locked until all of Day 1 is done.
 */
function isActionable(topic: Topic, completed: Set<string>): boolean {
  return (
    !completed.has(topic.id) &&
    topic.priority !== "LATER" &&
    topic.prerequisites.every((p) => completed.has(p))
  );
}

/** Order a block easy → hard so you warm up before the toughest topic (difficulty ramp). */
function rampByDifficulty(topics: Topic[]): Topic[] {
  // Array.sort is stable, so equal-difficulty topics keep their authored order.
  return [...topics].sort((a, b) => DIFFICULTY_RANK[a.difficulty] - DIFFICULTY_RANK[b.difficulty]);
}

/**
 * First-fit topics into a minute ceiling. `forceFirst` includes the first topic
 * even if it alone exceeds the budget — right for the FOCUS block (a single big
 * topic should still show), but off for the VARIETY spark, which must stay
 * within the time left over so the day doesn't blow past its budget.
 */
function fitToBudget(topics: Topic[], budget: number, maxTopics = Infinity, forceFirst = true): Topic[] {
  const picked: Topic[] = [];
  let used = 0;
  for (const topic of topics) {
    if (picked.length >= maxTopics) {
      break;
    }
    const fits = used + topic.estimatedMinutes <= budget;
    if (fits || (forceFirst && picked.length === 0)) {
      picked.push(topic);
      used += topic.estimatedMinutes;
    }
  }
  return picked;
}

/** Lightest-first — the variety spark should feel like a light change of pace. */
function byMinutesAsc(topics: Topic[]): Topic[] {
  return [...topics].sort((a, b) => a.estimatedMinutes - b.estimatedMinutes);
}

/** Builds per-module rollups (progress, actionable set) in authored order. */
function buildModuleInfos(
  roadmaps: Awaited<ReturnType<typeof loadRoadmaps>>,
  completed: Set<string>,
): ModuleInfo[] {
  const infos: ModuleInfo[] = [];
  let order = 0;
  for (const roadmap of roadmaps) {
    for (const course of roadmap.courses) {
      for (const module of course.modules) {
        const completedCount = module.topics.filter((t) => completed.has(t.id)).length;
        infos.push({
          moduleId: module.id,
          moduleTitle: module.title,
          courseId: course.id,
          courseTitle: course.title,
          order: order++,
          topics: module.topics,
          actionable: module.topics.filter((t) => isActionable(t, completed)),
          completedCount,
          totalCount: module.topics.length,
          inProgress: completedCount > 0 && completedCount < module.topics.length,
        });
      }
    }
  }
  return infos;
}

/**
 * Ranks modules for the FOCUS block, biased toward the GeoIntel AI track (the
 * primary mastery goal). Ordering, most-important key first:
 *   1. AI-track modules lead every other course.
 *   2. Within a tier, finish-what-you-started: an already-started module wins so
 *      you get closure and compounding depth before opening a new thread.
 *   3. Among started modules, the one closest to done.
 *   4. Otherwise the earliest unstarted module in the curriculum.
 * The day's focus starts at the top of this list and continues down it (across
 * modules, AI first) until the focus budget is spent — so a big budget covers
 * more of the AI track in a day rather than leaving the plan half-empty. Non-AI
 * tracks are reached only after the AI track has no actionable modules left;
 * until then they keep advancing via the daily variety spark.
 */
function rankFocusModules(modulesWithWork: ModuleInfo[]): ModuleInfo[] {
  return [...modulesWithWork].sort((a, b) => {
    const aBias = a.courseId === FOCUS_BIAS_COURSE_ID ? 0 : 1;
    const bBias = b.courseId === FOCUS_BIAS_COURSE_ID ? 0 : 1;
    if (aBias !== bBias) {
      return aBias - bBias; // AI track leads
    }
    if (a.inProgress !== b.inProgress) {
      return a.inProgress ? -1 : 1; // finish what you started
    }
    if (a.inProgress && b.inProgress) {
      const ratio = b.completedCount / b.totalCount - a.completedCount / a.totalCount;
      if (ratio !== 0) {
        return ratio; // closest to done first
      }
    }
    return a.order - b.order; // earliest in the curriculum
  });
}

/**
 * Picks the VARIETY module — a genuine change of pace from a *different course*
 * than the focus (falling back to a different module in the same course). Among
 * candidate courses it rotates by day so every track advances over time instead
 * of the same one always getting the spark; within the chosen course it again
 * prefers finishing a started module.
 */
function pickVarietyModule(
  modulesWithWork: ModuleInfo[],
  focus: ModuleInfo,
  daySeed: number,
  remaining: number,
): ModuleInfo | null {
  const fits = (m: ModuleInfo) => m.actionable.some((t) => t.estimatedMinutes <= remaining);
  let candidates = modulesWithWork.filter((m) => m.courseId !== focus.courseId && fits(m));
  if (candidates.length === 0) {
    candidates = modulesWithWork.filter((m) => m.moduleId !== focus.moduleId && fits(m));
  }
  if (candidates.length === 0) {
    return null;
  }

  // Rotate across the distinct candidate courses so variety spreads day to day.
  const courseOrder: string[] = [];
  for (const m of candidates) {
    if (!courseOrder.includes(m.courseId)) {
      courseOrder.push(m.courseId);
    }
  }
  const chosenCourse = courseOrder[daySeed % courseOrder.length];
  const inCourse = candidates.filter((m) => m.courseId === chosenCourse);

  const started = inCourse
    .filter((m) => m.inProgress)
    .sort((a, b) => b.completedCount / b.totalCount - a.completedCount / a.totalCount || a.order - b.order);
  return started[0] ?? inCourse[0];
}

/**
 * Spaced review: resurface one hard (non-beginner) topic finished 3–10 days ago
 * for a quick recall pass, so mastery sets long-term. Rotates by day so the same
 * topic isn't nagged every day, and prefers the most-overdue one.
 */
function pickReviewTopic(
  allTopics: Topic[],
  progress: ReturnType<typeof getAllProgress>,
  todayMs: number,
  daySeed: number,
): { topic: Topic; daysAgo: number } | null {
  const due: { topic: Topic; daysAgo: number }[] = [];
  for (const topic of allTopics) {
    if (topic.difficulty === "BEGINNER") {
      continue;
    }
    const p = progress[topic.id];
    if (!p || p.status !== "COMPLETED" || !p.completedDate) {
      continue;
    }
    const daysAgo = Math.round((todayMs - Date.parse(p.completedDate)) / MS_PER_DAY);
    if (daysAgo >= REVIEW_MIN_DAYS && daysAgo <= REVIEW_MAX_DAYS) {
      due.push({ topic, daysAgo });
    }
  }
  if (due.length === 0) {
    return null;
  }
  due.sort((a, b) => b.daysAgo - a.daysAgo);
  return due[daySeed % due.length];
}

function focusReason(module: ModuleInfo): string {
  if (module.inProgress) {
    return `You're ${module.completedCount}/${module.totalCount} through "${module.moduleTitle}" — finishing it now gives you a connected, in-depth grasp before opening anything new.`;
  }
  return `Starting "${module.moduleTitle}". Building this foundation now is what makes the later ${module.courseTitle} topics click instead of confuse.`;
}

function varietyReason(module: ModuleInfo, focus: ModuleInfo): string {
  return `A change of pace from ${focus.courseTitle} into ${module.courseTitle} — a little interleaving keeps you fresh and actually helps today's focus stick.`;
}

function reviewReason(daysAgo: number): string {
  return `A ${daysAgo}-day-old topic, resurfaced on purpose. One quick recall pass now is where short-term learning turns into lasting mastery.`;
}

/** Resolves stored id lists back to live Topic objects (picking up fresh progress). */
function resolveStoredPlan(stored: StoredPlan, byId: Map<string, Topic>): TodaysPlan {
  const sections: PlanSection[] = stored.sections
    .map((s) => ({
      kind: s.kind,
      title: s.title,
      courseTitle: s.courseTitle,
      reason: s.reason,
      topics: s.topicIds.map((id) => byId.get(id)).filter((t): t is Topic => Boolean(t)),
    }))
    .filter((s) => s.topics.length > 0);

  const topics = sections
    .filter((s) => s.kind !== "REVIEW")
    .flatMap((s) => s.topics);

  return { date: stored.date, dailyMinutes: stored.dailyMinutes, sections, topics };
}

function toStoredSection(section: PlanSection): StoredPlanSection {
  return {
    kind: section.kind,
    title: section.title,
    courseTitle: section.courseTitle,
    reason: section.reason,
    topicIds: section.topics.map((t) => t.id),
  };
}

/**
 * The adaptive rule-based planner. No day-by-day schedule is stored ahead of
 * time — today's plan is derived each day from the topic pool (static syllabus
 * JSON), progress (localStorage), and the daily time budget.
 *
 * The shape of a day is "deep focus + a variety spark":
 *   1. REVIEW  — one quick recall of a hard topic finished a few days ago
 *                (spaced repetition), only on days with room to spare.
 *   2. FOCUS   — the bulk of the day on the AI track, prioritising a module
 *                you've already started (finish-what-you-started), ordered
 *                easy → hard (difficulty ramp) so you build momentum. On larger
 *                budgets it continues into the next AI modules, each its own
 *                labelled block, so the day fills with depth.
 *   3. VARIETY — one taste of a different track to stay interested; the track
 *                rotates day to day so breadth still advances.
 * Every block carries a one-line "why this" reason so the plan never feels
 * random — the thing that keeps you oriented instead of confused.
 *
 * Once computed, the plan is persisted for the date (with its budget) so it
 * stays stable through the day; a same-budget refresh reuses it, while a budget
 * change recomputes at the new size. Topics finished *today* are kept visible in
 * their block so a re-plan still shows the day's wins.
 */
export async function computeTodaysPlan(): Promise<TodaysPlan> {
  const date = todayIso();
  const dailyMinutes = getDailyMinutes();
  const roadmaps = await loadRoadmaps();
  const allTopics = flattenTopics(roadmaps);
  const byId = new Map(allTopics.map((t) => [t.id, t]));

  const stored = getStoredPlan(date);
  if (stored && stored.dailyMinutes === dailyMinutes) {
    return resolveStoredPlan(stored, byId);
  }

  const progress = getAllProgress();
  const completed = new Set(allTopics.filter((t) => t.completionStatus === "COMPLETED").map((t) => t.id));
  const completedToday = (t: Topic) =>
    t.completionStatus === "COMPLETED" && t.completedDate === date;

  const daySeed = Math.floor(Date.parse(date) / MS_PER_DAY);
  const moduleInfos = buildModuleInfos(roadmaps, completed);
  const modulesWithWork = moduleInfos.filter((m) => m.actionable.length > 0);

  const allowExtras = dailyMinutes >= MIN_BUDGET_FOR_EXTRAS;
  let remaining = dailyMinutes;
  const sections: PlanSection[] = [];

  // 1. REVIEW (warm-up recall) — only when the day has room to spare.
  if (allowExtras) {
    const review = pickReviewTopic(allTopics, progress, Date.parse(date), daySeed);
    if (review) {
      sections.push({
        kind: "REVIEW",
        title: "Review — lock it in",
        courseTitle: null,
        reason: reviewReason(review.daysAgo),
        topics: [review.topic],
      });
      remaining -= Math.min(review.topic.estimatedMinutes, REVIEW_MAX_COST);
    }
  }

  const rankedFocus = rankFocusModules(modulesWithWork);
  const primaryFocus = rankedFocus[0] ?? null;

  // 2. FOCUS — the deep-work core of the day. It gets everything except a small
  // reserve for the variety spark, but never less than half the day. On larger
  // budgets it flows down the ranked list (AI track first) across consecutive
  // modules — each its own labelled block — so the day fills with real depth
  // instead of stopping when one module runs out.
  if (primaryFocus) {
    const focusBudget = allowExtras
      ? Math.max(remaining - VARIETY_RESERVE, Math.round(remaining * 0.5))
      : remaining;
    let focusUsed = 0;
    for (const module of rankedFocus) {
      if (focusUsed >= focusBudget) {
        break;
      }
      const isFirstBlock = focusUsed === 0;
      const moduleDoneToday = module.topics.filter(completedToday);
      // Only the very first block force-includes a topic (so the day is never
      // empty); later blocks add only what genuinely fits the budget left.
      const picked = fitToBudget(
        rampByDifficulty(module.actionable),
        focusBudget - focusUsed,
        Infinity,
        isFirstBlock,
      );
      const blockTopics = [...moduleDoneToday, ...picked];
      if (blockTopics.length > 0) {
        sections.push({
          kind: "FOCUS",
          title: module.moduleTitle,
          courseTitle: module.courseTitle,
          reason: focusReason(module),
          topics: blockTopics,
        });
      }
      focusUsed += picked.reduce((sum, t) => sum + t.estimatedMinutes, 0);
      // Nothing from this module fit the remaining budget — later ones won't
      // either in practice, so stop opening new blocks.
      if (picked.length === 0 && !isFirstBlock) {
        break;
      }
    }
    remaining -= focusUsed;
  }

  // 3. VARIETY — a rotating spark from another track, on days with room left.
  if (primaryFocus && allowExtras && remaining >= 10) {
    const variety = pickVarietyModule(modulesWithWork, primaryFocus, daySeed, remaining);
    if (variety) {
      const varietyDoneToday = variety.topics.filter(completedToday);
      const varietyPicked = fitToBudget(byMinutesAsc(variety.actionable), remaining, MAX_VARIETY_TOPICS, false);
      const varietyTopics = [...varietyDoneToday, ...varietyPicked];
      if (varietyTopics.length > 0) {
        sections.push({
          kind: "VARIETY",
          title: variety.moduleTitle,
          courseTitle: variety.courseTitle,
          reason: varietyReason(variety, primaryFocus),
          topics: varietyTopics,
        });
      }
    }
  }

  const plan: StoredPlan = {
    date,
    dailyMinutes,
    sections: sections.map(toStoredSection),
  };
  setStoredPlan(date, plan);

  const topics = sections.filter((s) => s.kind !== "REVIEW").flatMap((s) => s.topics);
  return { date, dailyMinutes, sections, topics };
}
