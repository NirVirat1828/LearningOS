const SETTINGS_KEY = "learningos.settings.v1";
// v2: the stored plan is now a structured, sectioned object (see StoredPlan)
// instead of a flat id list, so the day's focus/variety/review grouping and
// its "why this" reasons stay stable through the day. The bump means any old
// flat-array plan is simply ignored and recomputed once.
const PLAN_PREFIX = "learningos.plan.v2.";

const DEFAULT_DAILY_MINUTES = 180;

interface Settings {
  dailyMinutes: number;
}

function readSettings(): Settings {
  try {
    const raw = localStorage.getItem(SETTINGS_KEY);
    if (raw) {
      return JSON.parse(raw) as Settings;
    }
  } catch {
    /* fall through to default */
  }
  return { dailyMinutes: DEFAULT_DAILY_MINUTES };
}

export function getDailyMinutes(): number {
  const value = readSettings().dailyMinutes;
  return Number.isFinite(value) && value > 0 ? value : DEFAULT_DAILY_MINUTES;
}

export function setDailyMinutes(minutes: number): void {
  localStorage.setItem(SETTINGS_KEY, JSON.stringify({ dailyMinutes: minutes }));
}

/** One grouped block of today's plan, persisted by topic id (see StoredPlan). */
export interface StoredPlanSection {
  kind: "REVIEW" | "FOCUS" | "VARIETY";
  title: string;
  courseTitle: string | null;
  reason: string;
  topicIds: string[];
}

/**
 * Today's plan is persisted (for one date) so it stays a stable, finishable
 * list through the day even as topics get checked off — rather than reshuffling
 * on every refresh. `dailyMinutes` is stored alongside so the planner can tell a
 * same-budget refresh (reuse the plan) from a budget change (recompute at the
 * new size). Carry-forward is implicit: an unfinished topic is still actionable
 * tomorrow, so the next day's fresh computation simply picks it again.
 */
export interface StoredPlan {
  date: string;
  dailyMinutes: number;
  sections: StoredPlanSection[];
}

export function getStoredPlan(date: string): StoredPlan | null {
  try {
    const raw = localStorage.getItem(PLAN_PREFIX + date);
    if (!raw) {
      return null;
    }
    const parsed = JSON.parse(raw) as StoredPlan;
    // Guard against a half-written or older-shaped value.
    return Array.isArray(parsed.sections) ? parsed : null;
  } catch {
    return null;
  }
}

export function setStoredPlan(date: string, plan: StoredPlan): void {
  localStorage.setItem(PLAN_PREFIX + date, JSON.stringify(plan));
}

/** Drop the stored plan for a date so it gets recomputed — used when the budget changes. */
export function clearStoredPlan(date: string): void {
  localStorage.removeItem(PLAN_PREFIX + date);
}

export function todayIso(): string {
  return new Date().toISOString().slice(0, 10);
}
